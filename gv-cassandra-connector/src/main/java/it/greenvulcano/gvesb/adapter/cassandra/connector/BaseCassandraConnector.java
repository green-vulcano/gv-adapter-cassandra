package it.greenvulcano.gvesb.adapter.cassandra.connector;

import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.Session;
import it.greenvulcano.gvesb.adapter.cassandra.CassandraConnector;

public class BaseCassandraConnector implements CassandraConnector {
	
	private static final Logger LOG = LoggerFactory.getLogger(BaseCassandraConnector.class);
	
	
	private String contactPoints = "localhost";
	private int port = ProtocolOptions.DEFAULT_PORT;	
		
	private String name, user, password, defaultKeyspace;
	
	private Cluster cluster;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactPoints() {
		return contactPoints;
	}

	public void setContactPoints(String contactPoints) {
		this.contactPoints = contactPoints;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDefaultKeyspace() {
		return defaultKeyspace;
	}

	public void setDefaultKeyspace(String defaultKeyspace) {
		this.defaultKeyspace = defaultKeyspace;
	}

	public void init() {
		
		LOG.debug("Building Cluster instance "+ Objects.requireNonNull(name));
		
				
		AuthProvider authProvider = Objects.nonNull(user) && !user.trim().isEmpty()?
											       new PlainTextAuthProvider(user, password):
											       AuthProvider.NONE;
		
		cluster = Cluster.builder()
				         .withClusterName(name)
						 .addContactPoints(contactPoints.split(","))
						 .withPort(port)
						 .withAuthProvider(authProvider)
						 .build();	
		
	}
	
	
	public void destroy() {
		cluster.close();
	}
	
	
	@Override
	public Session getSession() {
		LOG.debug("Building session instance, on cluster "+name+ " -  keyspace: "+defaultKeyspace);
		Session session = Optional.ofNullable(defaultKeyspace).map(cluster::connect).orElseGet(cluster::connect);
				
		return session;
	}
	
	

}
