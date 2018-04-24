package it.greenvulcano.gvesb.virtual.cassandra.mapping;

import org.json.JSONArray;
import org.json.JSONObject;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import it.greenvulcano.gvesb.virtual.cassandra.CassandraQueryCallOperation.ResultSetMapper;

public class JSONResultSetMapper implements ResultSetMapper<JSONArray> {

	@Override
	public JSONArray map(ResultSet resultSet) {
		
		JSONArray jsonArray = new JSONArray();
		
		for (Row row : resultSet) {
		
			JSONObject item = new JSONObject();
			
			for (Definition column : row.getColumnDefinitions()) {
			
				String columnName = column.getName();
				
				DataType dataType = column.getType();
				
				if (dataType.equals(DataType.bigint())) {					
					item.put(columnName, row.getLong(columnName));
				} else if (dataType.equals(DataType.cint())||dataType.equals(DataType.varint())||dataType.equals(DataType.smallint())||dataType.equals(DataType.tinyint())) {
					item.put(columnName, row.getInt(columnName));
				} else if (dataType.equals(DataType.decimal())||dataType.equals(DataType.cfloat())) {
					item.put(columnName, row.getFloat(columnName));
				} else if (dataType.equals(DataType.cdouble())) {
					item.put(columnName, row.getDouble(columnName));
				} else if (dataType.equals(DataType.cboolean())) {
					item.put(columnName, row.getBool(columnName));
				} else if (dataType.equals(DataType.ascii())||dataType.equals(DataType.text())||dataType.equals(DataType.varchar())) {
					item.put(columnName, row.getString(columnName).toString());
				} else {
					item.put(columnName, row.getObject(columnName).toString());
				}
				
			}
			
			jsonArray.put(item);
		
		}			
		
		return jsonArray;
	}

}
