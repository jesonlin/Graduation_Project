package com.linjinzhong.musicplayer.Utils;

import java.io.Serializable;

public class MusicDemo implements Serializable {
	/**歌曲中的音乐列表样式*/
	/**
	Serialization（序列化）是一种将对象以一连串的字节描述的过程；
	反序列化Deserialization是一种将这些字节重建成一个对象的过程。
	 */
	
	private static final long serialVersionUID = 1L;
	private String name;
	private String path;
	private String artist;

	public MusicDemo(){
		// TODO Auto-generated constructor stub
	}
	
	public MusicDemo(String name,String path,String artist){
		super();
		this.name=name;
		this.path=path;
		this.artist=artist;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "MusicDemo [name=" + name + ", path=" + path + ", artist=" + artist + "]";
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getPath(){
		return path;
	}

	public void setPath(String path){
		this.path=path;
	}
	
	public String getArtist(){
		return artist;
	}
	
	public void setArtist(String artist){
		this.artist=artist;
	}
}
