/**
 *
 */
package steven.nextg.database.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import steven.nextg.database.vo.OracleConnectionProperties;

/**
 * @author steven.lam.t.f
 *
 */
public class TnsnamesUtils{
	public static final OracleConnectionProperties[] load() throws IOException{
		return TnsnamesUtils.load(new File(System.getenv("TNS_ADMIN") + File.separator + "tnsnames.ora"));
	}
	@SuppressWarnings("unchecked")
	public static final OracleConnectionProperties[] load(final File tnsnames) throws IOException{
		try(final InputStream is = new FileInputStream(tnsnames); final Reader r = new InputStreamReader(is, "UTF8"); final BufferedReader br = new BufferedReader(r);){
			final List<OracleConnectionProperties> results = new ArrayList<>();
			String line = null;
			final StringBuilder sb = new StringBuilder();
			final List<KeyValue> stack = new ArrayList<>();
			boolean key = true;
			while((line = br.readLine()) != null){
				line = line.replace(" ", "").replace("\t", "").toLowerCase();
				if(line.startsWith("#") == false){
					sb.append(line);
					boolean pass = true;
					while(pass && sb.length() > 0){
						if(key){
							final int equalIndex = sb.indexOf("=");
							if(equalIndex >= 0){
								stack.add(new KeyValue(sb.substring(0, equalIndex)));
								sb.delete(0, equalIndex + 1);
								key = false;
							}else{
								pass = false;
							}
						}else if(sb.charAt(0) == '('){
							sb.delete(0, 1);
							key = true;
						}else if(sb.charAt(0) == ')'){
							sb.delete(0, 1);
							final KeyValue current = stack.remove(stack.size() - 1);
							final KeyValue top = stack.get(stack.size() - 1);
							if(top.value == null){
								top.value = current;
							}else if(top.value instanceof KeyValue){
								final List<KeyValue> list = new ArrayList<>();
								list.add((KeyValue)top.value);
								list.add(current);
								top.value = list;
							}else{
								((List<KeyValue>)top.value).add(current);
							}
							if(stack.size() == 1){
								key = true;
								final String name = top.key.replace(".world", "");
								String host = null;
								String port = null;
								String sid = null;
								while(stack.size() > 0){
									final KeyValue kv = stack.remove(0);
									if(kv.value instanceof String){
										if("host".equals(kv.key)){
											host = (String)kv.value;
										}else if("port".equals(kv.key)){
											port = (String)kv.value;
										}else if("sid".equals(kv.key)){
											sid = (String)kv.value;
										}else if("service_name".equals(kv.key)){
											sid = (String)kv.value;
										}
									}else if(kv.value instanceof List){
										stack.addAll((List<KeyValue>)kv.value);
									}else if(kv.value instanceof KeyValue){
										stack.add((KeyValue)kv.value);
									}
								}
								results.add(new OracleConnectionProperties(name, host, Integer.parseInt(port), sid));
							}
						}else{
							final int closeIndex = sb.indexOf(")");
							if(closeIndex >= 0){
								stack.get(stack.size() - 1).value = sb.substring(0, closeIndex);
								sb.delete(0, closeIndex);
							}else{
								pass = false;
							}
						}
					}
				}
			}
			return results.toArray(new OracleConnectionProperties[results.size()]);
		}
	}

	private static final class KeyValue{
		private final String key;
		private Object value;

		private KeyValue(final String key){
			this.key = key;
		}
		@Override
		public String toString(){
			return this.key + "=" + this.value;
		}
	}
}
