[![Build Status](https://travis-ci.com/trustpilot/datalake-kafka-connect-dynamodb.svg?token=fhWdHVx5wocrF4axEby7&branch=master)](https://travis-ci.com/trustpilot/datalake-kafka-connect-dynamodb)

# kafka-connect-dynamodb

A plug-in for the [Kafka Connect framework](https://kafka.apache.org/documentation.html#connect) which implements a "source connector" for DynamoDB table Streams. This source connector extends [Confluent ecosystem](https://www.confluent.io/hub/) and allows replicating DynamoDB tables into Kafka topics. Once data is in Kafka you can use various Confluent sink connectors to push this data to different destinations systems for e.g. into BigQuery for easy analytics.  

## Additional features
* `autodiscovery` - monitors and automatically discovers DynamoDB tables to start/stop syncing from (based on AWS  TAG's)
* `INIT_SYNC` - automatically detects and if needed performs initial(existing) data replication before continuing tracking changes from the Stream
  
## Alternatives 

We found only one existing alternative implementation by [shikhar](https://github.com/shikhar/kafka-connect-dynamodb), but it seems to be missing major features and is no longer supported. 

Also it tries to manage DynamoDB Stream shards manually by using one Connect task to read from each DynamoDB Streams shard, but since DynamoDB Stream shards are dynamic compared to static ones in Kinesis streams this approach would require rebalancing all Confluent Connect cluster tasks far to often.

Contrary in our implementation we opted to use Amazon Kinesis Client with DynamoDB Streams Kinesis Adapter which takes care of all shard reading and tracking tasks.



## Built with

* Java 8
* Gradlew 5.3.1
* Confluent Kafka Connect Framework >= 2.1.1
* Amazon Kinesis Client 1.9.1
* DynamoDB Streams Kinesis Adapter 1.4.0

## Documentation
* [Getting started](docs/getting-started.md)
* [In depth explanation](docs/details.md)
* [Connector option](docs/options.md)
* [Produced Kafka messages](docs/data.md)

## Usage considerations, requirements and limitations

* KCL(Amazon Kinesis Client) keeps metadata in separate dedicated DynamoDB table for each DynamoDB Stream it's tracking. Meaning that there will be one additional table created for each table this connector is tracking.
  
* Current implementation supports only one Confluent task(= KCL worker) reading from one table at any given time. 
  * This limits maximum throughput possible from one table to about **~2000 records(change events) per second**.
  * This limitation is imposed by current plugin logic and not by the KCL library or Kafka connect framework. Running multiple tasks would require additional synchronization mechanisms for `INIT SYNC` state tracking and might be implemented in feature.
  
* Running multiple tasks for different tables on the same JVM has negative impact on overall performance of both tasks.
  * This is so because Amazon Kinesis Client library has some global locking happening. 
  * This issue has been solved in newer KCL versions, but reading from DynamoDB Streams requires usage of DynamoDB Streams Kinesis Adapter library. And this library still depends on older Amazon Kinesis Client 1.9.1.
  * That being said you will only encounter this issue by running lots of tasks on one machine with really high load.

* DynamoDB table unit capacity must be large enough to enable `INIT_SYNC` to finished in around 16 hours. Otherwise you risk `INIT_SYNC` being restarted just as soon as it's finished because DynamoDB Streams store change events only for 24 hours.

* Required AWS roles:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "dynamodb:DescribeTable",
                "dynamodb:DescribeStream",
                "dynamodb:ListTagsOfResource",
                "dynamodb:DescribeLimits"
                "dynamodb:GetRecords",
                "dynamodb:GetShardIterator", 
                "dynamodb:Scan"
            ],
            "Resource": [
                "arn:aws:dynamodb:*:*:table/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "dynamodb:*"
            ],
            "Resource": [
                "arn:aws:dynamodb:*:*:table/datalake-KCL-*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "dynamodb:ListStreams",
                "dynamodb:ListTables",
                "dynamodb:ListGlobalTables",
                "tag:GetResources"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
```


## Development

```bash
#  build & run unit tests
./gradlew

# build final jar
./gradlew shadowJar
```

### Debugging 

To interactively debug your connector thirst:
* complete steps defined in [Getting started](docs/getting-started.md)
* set following variables:
  ```bash
  confluent stop connect
  export CONNECT_DEBUG=y; export DEBUG_SUSPEND_FLAG=y;
  confluent start connect
  ```
* Connect with your debugger. For instance remotely to the Connect worker (default port: 5005) 
* To stop running connect in debug mode, just run: ```unset CONNECT_DEBUG; unset DEBUG_SUSPEND_FLAG;``` when you are done

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the tags on this repository.
 
## Releases

Releases are done by creating new release(aka tag) via Github user interface. Once created Travis will pick it up, build and upload final .jar file as asset for the Github release.

## Roadmap  (TODO: move to issues?)

* Add Confluent stack as docker-compose.yml for easier local debugging
* Use multithreaded DynamoDB table scan for faster `INIT SYNC`  


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* [Confluent](https://www.confluent.io/)
* [Debezium](https://debezium.io/)
* [kafka-connect-couchbase](https://github.com/couchbase/kafka-connect-couchbase)
* [amazon-kinesis-client](https://github.com/awslabs/amazon-kinesis-client)
* [dynamodb-streams-kinesis-adapter](https://github.com/awslabs/dynamodb-streams-kinesis-adapter)