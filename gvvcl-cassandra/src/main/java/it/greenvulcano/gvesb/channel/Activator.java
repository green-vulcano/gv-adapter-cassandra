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
package it.greenvulcano.gvesb.channel;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.cassandra.CassandraMetadataCallOperation;
import it.greenvulcano.gvesb.virtual.cassandra.CassandraQueryCallOperation;

public class Activator implements BundleActivator {
	private final static Logger LOG = LoggerFactory.getLogger(Activator.class);
	
	private final static ConfigurationListener configurationListener = event-> {
		
		LOG.debug("GV ESB Kafka plugin module - handling configuration event");
		
		if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(GreenVulcanoConfig.getSystemsConfigFileName())) {
			CassandraChannel.tearDown();
		}
		
		if ((event.getCode() == ConfigurationEvent.EVT_FILE_LOADED) && event.getFile().equals(GreenVulcanoConfig.getSystemsConfigFileName())) {
			CassandraChannel.setUp();
		}
	};

	@Override
	public void start(BundleContext context) throws Exception {
		OperationFactory.registerSupplier("cassandra-query-call", CassandraQueryCallOperation::new);
		OperationFactory.registerSupplier("cassandra-metadata-call", CassandraMetadataCallOperation::new);
		
		CassandraChannel.setUp();		
		XMLConfig.addConfigurationListener(configurationListener, GreenVulcanoConfig.getSystemsConfigFileName());

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
		XMLConfig.removeConfigurationListener(configurationListener);		
		CassandraChannel.tearDown();
		OperationFactory.unregisterSupplier("cassandra-query-call");
		OperationFactory.unregisterSupplier("cassandra-metadata-call");

	}

}
