package it.greenvulcano.gvesb.adapter.cassandra;

import com.datastax.driver.core.Session;

public interface CassandraConnector {
	
	Session getSession();

}
