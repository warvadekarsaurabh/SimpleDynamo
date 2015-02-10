package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
	static final String PROVIDER_NAME = "edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoActivity";
	static final String URL ="content://edu.buffalo.cse.cse486586.simpledynamo.provider";
	static final Uri CONTENT_URI = Uri.parse(URL);
	static final String KEY = "key";
	static final String VALUE = "value";
	private SQLiteDatabase MyDb;
	static String MyDbPath="/data/data/edu.buffalo.cse.cse486586.simpledynamo/databases/MyDataBase";
	static final String Table = "MyTable";	
	static final String Database = "MyDataBase";	
	static int count=0;
	static final int Version = 1;
	static final String DATABASE_CREATE = " CREATE TABLE " + Table +
	"(key TEXT , " +"value TEXT);";
	static final String TAG = "testing";
	static int msg=1;
	static final String SEQUENCER_PORT="11108";
	static final int SERVER_PORT = 10000;
	static String myPort=null;
	static String NextPort=null;
	static TreeMap<String,String[]> t=new TreeMap<String,String[]>();
	//static int count=0;

	boolean SuccessorSet=false;

	private static class DataBaseCreator extends SQLiteOpenHelper{	    

		public DataBaseCreator(Context context)
		{
			super(context,Database, null, Version);
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}


		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop Table if exists"+Table);
			onCreate(db);
		}

	}

	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rowsDeleted=0;
		if(selection.equalsIgnoreCase("*"))
		{
			rowsDeleted = MyDb.delete(Table, null, null);
		}
		else if(selection.equalsIgnoreCase("@"))
		{
			rowsDeleted = MyDb.delete(Table, null, null);
		}
		else 
		{
			String selectionArgs1[]={selection};
			rowsDeleted = MyDb.delete(Table, "key=?", selectionArgs1);
			rowsDeleted = MyDb.delete(Table, null, null);
			String msgToSend= selection+"delete it";
			try {
				if(!(t.ceilingEntry(genHash(selection))==null))
				{
					String[] finalvalue = (String[]) t.get(t.ceilingKey(genHash(selection)));
					String []a=finalvalue[0].split(",");
					
					for(int i=3;i<a.length;i++)
					{  
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgToSend,a[i]);
						
					}

				}
				else 
				{

					String[] finalvalue = (String[]) t.get(t.firstKey());
					String []a=finalvalue[0].split(",");
					for(int i=3;i<a.length;i++)
					{ 
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgToSend,a[i]);
					}
					
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   	 
			
		}
		return rowsDeleted; 
	}
	public String curstostring (Cursor cursor)
	{
		StringBuffer sb=new StringBuffer();
		if (cursor.getCount()>0){
			if (cursor.moveToFirst())
			{
				do				
				{
					String key = cursor.getString(cursor.getColumnIndex("key"));
					String value = cursor.getString(cursor.getColumnIndex("value"));

					sb.append(key);
					sb.append("&");
					sb.append(value);
					sb.append("+");


				}while(cursor.moveToNext());
			}

		}
		return sb.toString();	

	}
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public Uri insert(Uri uri, ContentValues values) {

		String key=values.getAsString(KEY);
		String value=values.getAsString(VALUE);
		String msgToSend=values.toString()+"#";


		try {
			if(!(t.ceilingEntry(genHash(key))==null))
			{
				String[] finalvalue = (String[]) t.get(t.ceilingKey(genHash(key)));
				String []a=finalvalue[0].split(",");
				
				for(int i=2;i<a.length;i++)
				{  					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgToSend,a[i]);
					
				}

			}
			else 
			{

				String[] finalvalue = (String[]) t.get(t.firstKey());
				String []a=finalvalue[0].split(",");
				for(int i=2;i<a.length;i++)
				{  
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgToSend,a[i]);
				}
			}}catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		boolean chk=checkDataBase();
		TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		myPort = String.valueOf((Integer.parseInt(portStr) * 2));

		try 
		{ 
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

			Context context = getContext();
			DataBaseCreator dbCreator=new DataBaseCreator(context);
			MyDb=dbCreator.getWritableDatabase();
			t.put(genHash("5554"), new String[]{"11124,11112,11108,11116,11120"});
			t.put(genHash("5558"), new String[]{"11112,11108,11116,11120,11124"});
			t.put(genHash("5560"), new String[]{"11108,11116,11120,11124,11112"});
			t.put(genHash("5562"), new String[]{"11116,11120,11124,11112,11108"});
			t.put(genHash("5556"), new String[]{"11120,11124,11112,11108,11116"});
			
			String[] finalvalue = (String[]) t.get(genHash (Integer.toString(Integer.parseInt(myPort)/2)));
			String []a=finalvalue[0].split(",");
			if(chk==true)
			{
				for(int i=0;i<a.length-1;i++ )
				{  
					if((i==2))
					{
						continue; 
					}
					if(i==3)
					{
						
						new CreateOtherTask().execute(a[i]);
						
					}
					else
					{   
						new CreateOtherTask().execute(a[i]);
						try {
							Thread.sleep(2000);
						} catch(InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}
				}	
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		//return (MyDb==null)?false:true; 
	}
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(MyDbPath, null,SQLiteDatabase.OPEN_READONLY);
			checkDB.close();
			return true;
		} catch (SQLiteException e) {
			// database doesn't exist yet.
			return false;
		}
		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
	String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qbChat = new SQLiteQueryBuilder();
		qbChat.setTables(Table);   
		Cursor c = null;

		MyDb=new DataBaseCreator(getContext()).getReadableDatabase();
		if(selection.equalsIgnoreCase("*"))
		{	
			String[] col={"key","value"};
			MatrixCursor mc = new MatrixCursor(col);

			BufferedWriter bw_Client=null;
			BufferedReader br_Server=null;
			Socket socket=null;
			StringBuilder read=new StringBuilder();
			for (Map.Entry<String, String[]> e : t.entrySet()) 
			{
				String[] finalvalue = (String[]) e.getValue();
				String []a=finalvalue[0].split(",");

				try {

					socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(a[2]));			
					// TODO Auto-generated method stub

					bw_Client = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					bw_Client.write(selection+"\n");
					
					bw_Client.flush();
					
					br_Server=new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String readStr=br_Server.readLine();
					
					if(readStr!=null)
					{  
						read.append(readStr);
						String[] s=read.toString().split("\\+");
						for(int i=0;i<s.length;i++)
						{
							String[] s1=s[i].split("&");
							
							mc.addRow(new String[]{s1[0],s1[1]});
						}
					}
					

					else{
						
					}

				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
			

			try {
				br_Server.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				bw_Client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return mc;	
		}
		else if (selection.equalsIgnoreCase("@"))
		{
			c  = MyDb.rawQuery("SELECT * FROM MyTable",null);
		}
		else
		{	
			try {
				if(!(t.ceilingEntry(genHash(selection))==null))
				{
					String[] finalvalue = (String[]) t.get(t.ceilingKey(genHash(selection)));
					String []a=finalvalue[0].split(",");
					{
						Socket socket;
						try {
							socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(a[2]));

							// TODO Auto-generated method stub
							BufferedWriter bw_Client;
							bw_Client = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
							bw_Client.write(selection+"$"+"\n");
							bw_Client.flush();
							
							BufferedReader br_Server=new BufferedReader(new InputStreamReader(socket.getInputStream()));
							String read=br_Server.readLine();
							if(read!=null){
								String[] s=read.split("&");
								String[] col={"key","value"};
								MatrixCursor mc=new MatrixCursor(col);
								mc.addRow(new String[]{s[0],s[1]});
								br_Server.close();
								bw_Client.close();
								socket.close();
								return mc;
							}
							else
							{
								br_Server.close();
								bw_Client.close();
								socket.close();
								
								Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(a[3]));

								// TODO Auto-generated method stub
								BufferedWriter bwClient;
								bwClient = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
								bwClient.write(selection+"$"+"\n");
								
								bwClient.flush();
								
								BufferedReader brServer=new BufferedReader(new InputStreamReader(socket1.getInputStream()));
								String read1=brServer.readLine();

								String[] so=read1.split("&");
								String[] col={"key","value"};
								MatrixCursor mc=new MatrixCursor(col);
								mc.addRow(new String[]{so[0],so[1]});
								brServer.close();
								bwClient.close();
								socket1.close();
								return mc;

							}
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
				else 
				{
					String[] finalvalue = (String[]) t.get(t.firstKey());
					String []a=finalvalue[0].split(",");
					{

						Socket socket;
						try {
							socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(a[2]));

							// TODO Auto-generated method stub
							BufferedWriter bw_Client;
							bw_Client = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
							bw_Client.write(selection+"$"+"\n");
							bw_Client.flush();
							BufferedReader br_Server=new BufferedReader(new InputStreamReader(socket.getInputStream()));
							String read=br_Server.readLine();
							if(read!=null){
								String[] s=read.split("&");
								String[] col={"key","value"};
								MatrixCursor mc=new MatrixCursor(col);
								mc.addRow(new String[]{s[0],s[1]});
								br_Server.close();
								bw_Client.close();
								socket.close();
								return mc;
							}
							else
							{

								br_Server.close();
								bw_Client.close();
								socket.close();
								
								Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(a[3]));

								// TODO Auto-generated method stub
								BufferedWriter bwClient;
								bwClient = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
								bwClient.write(selection+"$"+"\n");
								
								bwClient.flush();
								BufferedReader brServer=new BufferedReader(new InputStreamReader(socket1.getInputStream()));
								String read1=brServer.readLine();

								String[] so=read1.split("&");
								String[] col={"key","value"};
								MatrixCursor mc=new MatrixCursor(col);
								mc.addRow(new String[]{so[0],so[1]});
								brServer.close();
								bwClient.close();
								socket1.close();
								return mc;

							}
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						

					}
				}
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		return c;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection,
	String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			// TODO Auto-generated method stub
			try 
			{
				while(true)
				{
					
					ServerSocket serverSocket = sockets[0];
					Socket socket=serverSocket.accept();  
					
					BufferedReader br_Server=new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String read=br_Server.readLine();

					if (read.contains("#"))
					{
						String[] split1=read.split(" ");
						String[] split2=split1[0].split("=");
						String[] split3=split1[1].split("=");
						String[] split4=split3[1].split("#");
						String Key=split4[0];
						String Value=split2[1];
						ContentValues newValue=new ContentValues();
						newValue.put(KEY,Key);
						newValue.put(VALUE,Value);

						
						Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",new String[]{Key});
						if(c.getCount()>0)
						{	c.moveToFirst();
							MyDb.delete("MyTable", "key=?" , new String[]{Key});
							
						}
						
						long rowId=MyDb.insert(Table,"", newValue);
						count++;
						
					}
					else if(read.contains("$"))
					{  
						String split1[]=read.split("\\$");
						String[] mselectionArgs={split1[0]};
						Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",mselectionArgs);
						BufferedWriter bw_query=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						if(c.getCount()>0)
						{
							c.moveToFirst();

							String key=c.getString(0);
							String value=c.getString(1);	
							bw_query.write(key+"&"+value+"\n");
							bw_query.flush();									
							bw_query.close();

						}

					}
					else if(read.equalsIgnoreCase("*"))
					{
						Cursor c = MyDb.rawQuery("SELECT * FROM MyTable",null); 
						String j=curstostring(c);
						{
							BufferedWriter bw_query=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
							
							bw_query.write(j+"\n");
							bw_query.flush();									
							bw_query.close();

						}
					}
					else if (read.contains("delete it"))
					{  						read=read.replaceAll("delete it","");
						
						String r[]={read};
						int rowsDeleted=0;
						rowsDeleted = MyDb.delete(Table, "key=?", r);
						rowsDeleted = MyDb.delete(Table, null, null);
						
						
					}


				}


			}
			catch (UnknownHostException e) {
				
			}
			catch (IOException e) 
			{

			}
			return null;
		}
	}

	private class ClientTask extends AsyncTask<String, Void, Void> {


		@Override
		protected Void doInBackground(String... msgs) {
			try {	
				Socket socket = null;
				
				socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(msgs[1]));
				BufferedWriter bw_Client;
				bw_Client = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				bw_Client.write(msgs[0]+"\n");
				bw_Client.flush();
				bw_Client.close();
				socket.close();
				return null;
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	private class CreateOtherTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... params) {


			try{

				String selection="*";
				BufferedWriter bw_Client=null;
				BufferedReader br_Server=null;
				Socket socket=null;

				socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(params[0]));			

				bw_Client = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				bw_Client.write(selection+"\n");

				bw_Client.flush();

				br_Server=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String read=br_Server.readLine();
				
				if(!(read==null || read.length()<15))
				{

					String[] s=read.toString().split("\\+");
					for(int i=0;i<s.length;i++)
					{

						String[] s1=s[i].split("&");

						String com=t.ceilingKey(genHash(s1[0]));
						int a= Integer.parseInt(params[0]);
						a=a/2;
						String po=Integer.toString(a);
						int b= Integer.parseInt(myPort);
						b=b/2;
						String mo=Integer.toString(b);
						if((com!=null))
						{
							if(    com.equalsIgnoreCase("177ccecaec32c54b82d5aaafc18a2dadb753e3b1") && (myPort.equalsIgnoreCase("11124")||myPort.equalsIgnoreCase("11112")||myPort.equalsIgnoreCase("11108")))
							{   
								String Key=s1[0];
								String Value=s1[1];
								ContentValues newValue=new ContentValues();
								newValue.put(KEY,Key);
								newValue.put(VALUE,Value);
								Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",new String[]{Key});
								if(c.getCount()>0)
								{ c.moveToFirst();
									MyDb.delete("MyTable", "key=?" , new String[]{Key});
									
								}
								
								long rowId=MyDb.insert(Table,"", newValue);
								
							}
							
							else if(    com.equalsIgnoreCase("208f7f72b198dadd244e61801abe1ec3a4857bc9") && (myPort.equalsIgnoreCase("11116")||myPort.equalsIgnoreCase("11112")||myPort.equalsIgnoreCase("11108")))
							{
								String Key=s1[0];
								String Value=s1[1];
								ContentValues newValue=new ContentValues();
								newValue.put(KEY,Key);
								newValue.put(VALUE,Value);
								Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",new String[]{Key});
								if(c.getCount()>0)
								{ c.moveToFirst();
									
									MyDb.delete("MyTable", "key=?" , new String[]{Key});
									
								}
								long rowId=MyDb.insert(Table,"", newValue);
								
							}
							else if(    com.equalsIgnoreCase("33d6357cfaaf0f72991b0ecd8c56da066613c089") && (myPort.equalsIgnoreCase("11120")||myPort.equalsIgnoreCase("11116")||myPort.equalsIgnoreCase("11108")))
							{  
								String Key=s1[0];
								String Value=s1[1];
								ContentValues newValue=new ContentValues();
								newValue.put(KEY,Key);
								newValue.put(VALUE,Value);
								Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",new String[]{Key});
								if(c.getCount()>0)
								{ c.moveToFirst();
									
									MyDb.delete("MyTable", "key=?" , new String[]{Key});
									
								}
								
								long rowId=MyDb.insert(Table,"", newValue);
								
							}
							else if(    com.equalsIgnoreCase("abf0fd8db03e5ecb199a9b82929e9db79b909643") && (myPort.equalsIgnoreCase("11124")||myPort.equalsIgnoreCase("11116")||myPort.equalsIgnoreCase("11120")))
							{  
								String Key=s1[0];
								String Value=s1[1];
								ContentValues newValue=new ContentValues();
								newValue.put(KEY,Key);
								newValue.put(VALUE,Value);
								Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",new String[]{Key});
								if(c.getCount()>0)
								{ c.moveToFirst();
									
									MyDb.delete("MyTable", "key=?" , new String[]{Key});
									
								}
								
								long rowId=MyDb.insert(Table,"", newValue);
								
							}
							else if(    com.equalsIgnoreCase("c25ddd596aa7c81fa12378fa725f706d54325d12") && (myPort.equalsIgnoreCase("11124")||myPort.equalsIgnoreCase("11112")||myPort.equalsIgnoreCase("11120")))
							{ 
								String Key=s1[0];
								String Value=s1[1];
								ContentValues newValue=new ContentValues();
								newValue.put(KEY,Key);
								newValue.put(VALUE,Value);
								Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",new String[]{Key});
								if(c.getCount()>0)
								{ c.moveToFirst();
									
									MyDb.delete("MyTable", "key=?" , new String[]{Key});
									
								}
								
								long rowId=MyDb.insert(Table,"", newValue);
								
							}

						}
						
						else 
						{ 
							try{	
								if(myPort.equalsIgnoreCase("11112")||myPort.equalsIgnoreCase("11124")|| myPort.equalsIgnoreCase("11108") )
								{
									
									String Key=s1[0];
									String Value=s1[1];
									ContentValues newValue=new ContentValues();
									newValue.put(KEY,Key);
									newValue.put(VALUE,Value);
									Cursor c = MyDb.rawQuery("SELECT * FROM MyTable WHERE key=?",new String[]{Key});
									if(c.getCount()>0)
									{ c.moveToFirst();
										
										MyDb.delete("MyTable", "key=?" , new String[]{Key});
										
									}
									
									long rowId=MyDb.insert(Table,"", newValue);
									
								}
							}catch(Exception e){
								e.printStackTrace();
								
							}
							
						}

					}



				}
				
				br_Server.close();
				bw_Client.close();
				socket.close();
			}catch(Exception e)
			{
				e.printStackTrace();

			}
			// TODO Auto-generated method stub
			return null;
		}
	}

}



