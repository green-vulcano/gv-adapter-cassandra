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
import it.greenvulcano.gvesb.virtual.cassandra.mapping.JSONMetadataMapper;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Objects;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

/**
 * 
 * @version 4.0 23/april/2018
 * @author GreenVulcano Developer Team
 */
public class CassandraMetadataCallOperation implements CallOperation {

	public interface MetadataMapper<T> {

		T map(Metadata metadata);

	}

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CassandraMetadataCallOperation.class);
	private OperationKey key = null;

	private String name, endpoint, keyspace;
	

	private MetadataMapper<?> mapper;

	@Override
	public void init(Node node) throws InitializationException {
		logger.debug("Init cassandra-query-call");
		try {
			name = XMLConfig.get(node, "@name");
			endpoint = Objects.requireNonNull(XMLConfig.get(node.getParentNode(), "@endpoint"), "Missing cluster connector endpoint");
			keyspace = XMLConfig.get(node, "@keyspace");
			
			mapper = new JSONMetadataMapper();
			
			logger.debug("Init cassandra-metadata-call " + name);

		} catch (Exception exc) {
			throw new InitializationException("GV_INIT_SERVICE_ERROR",
					new String[][] { { "message", exc.getMessage() } }, exc);
		}

	}

	@Override
	public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {

		try {
			
			String jndiname = PropertiesHandler.expand(this.endpoint, gvBuffer);
			String keyspace = PropertiesHandler.expand(this.keyspace, gvBuffer);
			
			Session session = Objects.requireNonNull(CassandraChannel.getSession(jndiname, keyspace), "Active session not found");

			gvBuffer.setObject(mapper.map(session.getCluster().getMetadata()));

		} catch (Exception exc) {
			throw new CallException("GV_CALL_SERVICE_ERROR",
					new String[][] { { "service", gvBuffer.getService() }, { "system", gvBuffer.getSystem() },
							{ "tid", gvBuffer.getId().toString() }, { "message", exc.getMessage() } },
					exc);
		}
		return gvBuffer;
	}

	@Override
	public void cleanUp() {
		// do nothing
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public String getServiceAlias(GVBuffer gvBuffer) {
		return gvBuffer.getService();
	}

	@Override
	public void setKey(OperationKey key) {
		this.key = key;
	}

	@Override
	public OperationKey getKey() {
		return key;
	}
}
