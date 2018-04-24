/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.cassandra;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.channel.CassandraChannel;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.cassandra.mapping.JSONResultSetMapper;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Objects;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;


/**
 * 
 * @version 4.0 23/april/2018
 * @author GreenVulcano Developer Team
 */
public class CassandraQueryCallOperation implements CallOperation {
	
	public interface ResultSetMapper<T> {
		
		 T map(ResultSet resultSet);		 
		 
		
	}
    
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CassandraQueryCallOperation.class);    
    private OperationKey key = null;
    
    private String name, statement;       
    private Session session;
    
    private ResultSetMapper<?> mapper;
    
    @Override
    public void init(Node node) throws InitializationException  {
        logger.debug("Init cassandra-query-call");
        try {            
            name =  XMLConfig.get(node, "@name");
            
            logger.debug("Init cassandra-query-call "+name);
            
            statement = XMLConfig.get(node, "./statement/text()");            
            session = Objects.requireNonNull(CassandraChannel.getSession(node), "Active session not found");
        	
            mapper = new JSONResultSetMapper();
            
        } catch (Exception exc) {
            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }
           

    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {
       
        try {	         
        	
        	String query = PropertiesHandler.expand(statement, gvBuffer);        	
        	ResultSet queryResult = session.execute(query);        	
        	        	
        	gvBuffer.setObject(mapper.map(queryResult));        	
           
        } catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{{"service", gvBuffer.getService()},
                    {"system", gvBuffer.getSystem()}, {"tid", gvBuffer.getId().toString()},
                    {"message", exc.getMessage()}}, exc);
        }
        return gvBuffer;
    }    
   
    @Override
    public void cleanUp(){
        // do nothing
    }
    
    @Override
    public void destroy(){
        // do nothing
    }

    @Override
    public String getServiceAlias(GVBuffer gvBuffer){
        return gvBuffer.getService();
    }

    @Override
    public void setKey(OperationKey key){
        this.key = key;
    }
    
    @Override
    public OperationKey getKey(){
        return key;
    }
}
