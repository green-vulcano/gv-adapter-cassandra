package it.greenvulcano.gvesb.channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.datastax.driver.core.Session;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.adapter.cassandra.CassandraConnector;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.util.xpath.XPathFinder;


public class CassandraChannel {
	
public static final String HEADER_TAG = "KAKFA_HEADER_";
	
	private final static Logger LOG = LoggerFactory.getLogger(CassandraChannel.class);
	
	private final static AtomicBoolean running = new AtomicBoolean(false);
	private final static ConcurrentMap<String, Session> cassandraSessions = new ConcurrentHashMap<>();
	
	private final static JNDIHelper jndiContext = new JNDIHelper();
	
	static void setUp() {
		
		if (running.compareAndSet(false, true)) {			
				
			LOG.debug("Inizialiting GV ESB Cassandra plugin module");
			
			try {
				NodeList cassandraChannelList = XMLConfig.getNodeList(GreenVulcanoConfig.getSystemsConfigFileName(),"//Channel[@type='CassandraAdapter' and @enabled='true']");
			
				LOG.debug("Enabled CassandraAdapter channels found: "+cassandraChannelList.getLength());
				
				IntStream.range(0, cassandraChannelList.getLength())
		         .mapToObj(cassandraChannelList::item)
		         .forEach(CassandraChannel::buildSession);
				
			} catch (XMLConfigException e) {
				LOG.error("Error reading configuration", e);
			}
		} else {
			LOG.debug("GV ESB Cassandra plugin module already inizialized");
		}
			
		
	}
	
	static void tearDown() {
		if (running.compareAndSet(true, false)) {
			LOG.debug("Finalizing GV ESB Cassandra plugin module");
			
			cassandraSessions.values().forEach(Session::close);
		
		}
	}
	
	private static void buildSession(Node cassandraChannel) {
	
		try {				
		
			CassandraConnector cassandraConnector = (CassandraConnector) jndiContext.lookup(XMLConfig.get(cassandraChannel, "@endpoint"));
							
			Session session = cassandraConnector.getSession();			
			cassandraSessions.putIfAbsent(XPathFinder.buildXPath(cassandraChannel), session);
			
		
		} catch (NamingException e) {
			LOG.error("Error retrieving CassandraConnector instance", e);
		} catch (XMLConfigException e) {
			LOG.error("Error reading configuration", e);
		}
	}
	
	public static Session getSession(Node operationNode) {
		String sessionKey = XPathFinder.buildXPath(operationNode.getParentNode());
		return cassandraSessions.get(sessionKey);	
	}
	

}
