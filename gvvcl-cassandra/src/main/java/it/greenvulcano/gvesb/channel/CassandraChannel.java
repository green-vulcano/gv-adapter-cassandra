package it.greenvulcano.gvesb.channel;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datastax.driver.core.Session;
import it.greenvulcano.gvesb.adapter.cassandra.CassandraConnector;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;


public class CassandraChannel {
	
	private final static Logger LOG = LoggerFactory.getLogger(CassandraChannel.class);

	private final static ConcurrentMap<String, Session> cassandraSessions = new ConcurrentHashMap<>();	
	private final static JNDIHelper jndiContext = new JNDIHelper();
		
	static void shutdown() {
		cassandraSessions.values().stream().forEach(Session::closeAsync);
		cassandraSessions.clear();
	}

	
	public static Session getSession(String connectorJndiName, String keyspace) {
		
		String sessionKey =  Optional.ofNullable(keyspace).orElse("default")+"@"+connectorJndiName;
		
		if (!cassandraSessions.containsKey(sessionKey)) {
			try {				
				
				CassandraConnector cassandraConnector = (CassandraConnector) jndiContext.lookup(connectorJndiName);
								
				Session session = Optional.ofNullable(keyspace).map(cassandraConnector::getSession).orElseGet(cassandraConnector::getSession);			
				cassandraSessions.putIfAbsent(sessionKey, session);
				
			
			} catch (NamingException e) {
				LOG.error("Error retrieving CassandraConnector instance", e);
			} catch (Exception e) {
				LOG.error("Error building Cassandra session instance", e);
			} 
		}		
		
		return cassandraSessions.get(sessionKey);	
	}
	

}
