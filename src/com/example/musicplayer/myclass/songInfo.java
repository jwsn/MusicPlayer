package com.example.musicplayer.myclass;

import java.util.Date;


public class songInfo {
	private Long id;
	private String name;
	private String path;
	private Date addDate;
	private Date updateDate;
	private String playlistId;

	public String getPlaylistId() {
		return playlistId;
	}
  
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}

	public void setPlaylistId(String playlistId) {
		this.playlistId = playlistId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
