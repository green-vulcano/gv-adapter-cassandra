package it.greenvulcano.gvesb.virtual.cassandra.mapping;

import java.util.stream.Collector;
import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.FunctionMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.MaterializedViewMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UserType;

import it.greenvulcano.gvesb.virtual.cassandra.CassandraMetadataCallOperation.MetadataMapper;

public class JSONMetadataMapper implements MetadataMapper<JSONObject> {	
	
	
	@Override
	public JSONObject map(Metadata metadata) {		
		
		return metadata.getKeyspaces().stream()
		        	   .map(this::mapKeyspace)
		               .collect(JSONObject::new, (a,k)->a.put(k.getString("name"),k), (a1,a2)->a2.keySet().forEach(key-> a1.append(key, a2.get(key))));
		
		}
	
	
	private <T> Collector<T, JSONArray, JSONArray> toJSONArray() { 
		return Collector.of(JSONArray::new, JSONArray::put, (a,b) -> {a.forEach(b::put);return a;});
	}
	
	private JSONObject mapKeyspace(KeyspaceMetadata keyspaceMetadata) {
		
		JSONObject keyspace = new JSONObject();
		
		keyspace.put("name", keyspaceMetadata.getName());
		keyspace.put("functions", keyspaceMetadata.getFunctions().stream().map(FunctionMetadata::getSignature).collect(toJSONArray()));
		keyspace.put("materializedViews", keyspaceMetadata.getMaterializedViews().stream().map(MaterializedViewMetadata::getName).collect(toJSONArray()));
		keyspace.put("userTypes", keyspaceMetadata.getUserTypes().stream().map(UserType::getName).collect(toJSONArray()));
		keyspace.put("tables", keyspaceMetadata.getTables().stream().map(this::mapTable).collect(toJSONArray()));
		return keyspace;
		
	
	}
	
	private JSONObject mapTable(TableMetadata tableMetadata) {
		
		JSONObject table = new JSONObject();
		
		table.put("name", tableMetadata.getName());
		table.put("columns" , tableMetadata.getColumns()
				                           .stream()
										   .map(c-> {
												return new JSONObject()
												                .put("name", c.getName())
												                .put("type", c.getType().getName());
														
													} )
										   .collect(toJSONArray()));
		return table;
		
	}

}
