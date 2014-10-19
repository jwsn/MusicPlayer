package com.example.musicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

		private static final int VERSION=5;
		private static String DBNAME = "mydb";
		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}
		public DBHelper(Context context, String name,
				int version) {
			this(context, name, null, version);
			// TODO Auto-generated constructor stub
		}
		public DBHelper(Context context, String name) {
			
				
			// TODO Auto-generated constructor stubs
			
			this(context,name,VERSION);
			
		}
		public DBHelper(Context context)
		{
			this(context,DBNAME,VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase arg0) {
			// TODO Auto-generated method stub
			System.out.println("create a database");
			//arg0.execSQL("create table user(username fresher(30), " +
				//	"userpasswd varchar(30),position varvhar(30),gender nvarchar(1),hobby varchar(30),marriged varchar(10))");
			arg0.execSQL("create table history(id number(2),musicname varchar(30) ,path varchar(200))");
			arg0.execSQL("create table mycollect(collectmusicname varchar(50))");
			arg0.execSQL("CREATE TABLE IF NOT EXISTS filedownlog (id integer primary key autoincrement, downpath varchar(100), threadid INTEGER, downlength INTEGER)");
			arg0.execSQL("create table exitinfo(lastmusicname varchar(30) ,playmode varchar(30) )");
			arg0.execSQL("create table settings(shakeseting varchar(30),lrcshow varchar(30))");
			arg0.execSQL("create table musicwordfontsize(fontSize varchar(20))");
			//arg0.execSQL("create table electric(,electroicLocated varchar(30),electricRoomId String ,electricId String primary key)");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
			System.out.println("upgrade SQL data or Version");
			db.execSQL("DROP TABLE IF EXISTS history");
			db.execSQL("DROP TABLE IF EXISTS mycollect");
			db.execSQL("DROP TABLE IF EXISTS exitinfo");
			db.execSQL("DROP TABLE IF EXISTS filedownlog");
			db.execSQL("DROP TABLE IF EXISTS settings");
			db.execSQL("DROP TABLE IF EXISTS musicwordfontsize");
			onCreate(db);
			

			
		}
		

	}
