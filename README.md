# squirrel

Flink window word count example in Clojure, reading text data from a local socket
and printing counts every 5 seconds.

Run local tests:

```
lein test
```

Build the Flink job:

```
lein uberjar
```

Start a local Flink cluster and a local netcat server:

```
$FLINK_HOME/bin/start-cluster.sh
nc -l -p 9000
```

Run the Flink job:

```
$FLINK_HOME/bin/flink run target/squirrel-0.1.0-standalone.jar 9000
```

Check the results in Flink's logs:

```
tail -f $FLINK_HOME/log/flink-*-taskmanager-*.out
```
