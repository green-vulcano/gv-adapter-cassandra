# gv-adapter-cassandra
### GV ESB v4 adapter for Apache Cassandra

Provides integration with Cassandra DB, includes call operations:
 - `cassandra-metadata-call`: return a JSON representation of database structure
     ```
     {
      "keyspace": {
         "name" : <string>,
         "functions": [<array of strings>],
         "materializedViews": [<array of strings>],
         "userTypes": [<array of strings>],
         "tables"[ { 
                        "name" :  <string>,
                        "columns": [ {"name":<string>, "type":<string>, ...}]
                     }, ...
        ]

      }, ...
    }
    ```
 - `cassandra-query-call`: return a JSON representation of query resultset
    ```
    [
      {"col1" : "val1", "col2" : val2, ...},
      {"col1" : "val1", "col2" : val2, ...}, 
      ...
    ]
    ```
## Usage
  1. Install connector bundle: `bundle:install -s mvn:it.greenvulcano.gvesb.adapter/gv-cassandra-connector/4.0.0-SNAPSHOT` (requires native lib `libjffi-1.2.so` in `<KARAF_HOME>/lib`)
  2. Configure a db connection with blueprint:
     `deploy/my-cassandra-connector.xml`
     
  ```
     <blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0">

    <bean id="myCassandraConnector" init-method="init" destroy-method="destroy" class="it.greenvulcano.gvesb.adapter.cassandra.connector.BaseCassandraConnector">
        <property name="name" value="MY_CDB" />
        <property name="contactPoints" value="192.168.1.10" />
        <property name="user" value="cassandra" />
        <property name="password" value="cassandra" />
        <property name="defaultKeyspace" value="MY_KEYSPACE" />
    </bean>

    <service ref="myCassandraConnector" interface="it.greenvulcano.gvesb.adapter.cassandra.CassandraConnector">
        <service-properties>
            <entry key="name" value="my-cassandra-connector" />
            <entry key="osgi.jndi.service.name" value="cassandra-connector" />
        </service-properties>
    </service>

</blueprint>

```
 3. Install adapter:  install -s -l 92 mvn:it.greenvulcano.gvesb.adapter/gvvcl-cassandra/4.0.0-SNAPSHOT
 
 ## Configuration example:
 
 
```
 
 <System id-system="GreenVulcano" system-activation="on">
         <Channel enabled="true" endpoint="osgi:service/cassandra-connector"
                  id-channel="BigData" type="CassandraAdapter">
               
               <cassandra-metadata-call name="getKeyspaceInfo" type="call"/>
               
               <cassandra-query-call name="getData" type="call">
                   <statement>select * from system.sstable_activity</statement>
                </cassandra-query-call>
         </Channel>
  </System>
  
```
