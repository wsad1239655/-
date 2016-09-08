package model;

import java.io.Serializable;

public class Mp3Info implements Serializable{
	private long id; // ����ID
	private String title; // �������� 
	private String album; // ר�� 
	private long albumId;//ר��ID 
	private String displayName; //��ʾ���� 
	private String artist; // �������� 
	private long duration; // ����ʱ�� 
	private long size; // ������С 
	private String url; // ����·�� 
	private String lrcTitle; // �������
	private String lrcSize; // ��ʴ�С 
	
	private String albumName;//ר����
	private String smallAlumUrl;//Сͼ
	private String bigAlumUrl;//��ͼ
	private String musicId;//����ID
	private String lrcUrl;//��ʵ�ַ
	
	
	public String getAlbumName() {
		return albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
	public String getSmallAlumUrl() {
		return smallAlumUrl;
	}
	public void setSmallAlumUrl(String smallAlumUrl) {
		this.smallAlumUrl = smallAlumUrl;
	}
	public String getBigAlumUrl() {
		return bigAlumUrl;
	}
	public void setBigAlumUrl(String bigAlumUrl) {
		this.bigAlumUrl = bigAlumUrl;
	}
	public String getMusicId() {
		return musicId;
	}
	public void setMusicId(String musicId) {
		this.musicId = musicId;
	}
	public String getLrcUrl() {
		return lrcUrl;
	}
	public void setLrcUrl(String lrcUrl) {
		this.lrcUrl = lrcUrl;
	}
	
	@Override
	public String toString() {
		return "Mp3Info [id=" + id + ", title=" + title + ", album=" + album + ", albumId=" + albumId + ", displayName="
				+ displayName + ", artist=" + artist + ", duration=" + duration + ", size=" + size + ", url=" + url
				+ ", lrcTitle=" + lrcTitle + ", lrcSize=" + lrcSize + "]";
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public long getAlbumId() {
		return albumId;
	}
	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLrcTitle() {
		return lrcTitle;
	}
	public void setLrcTitle(String lrcTitle) {
		this.lrcTitle = lrcTitle;
	}
	public String getLrcSize() {
		return lrcSize;
	}
	public void setLrcSize(String lrcSize) {
		this.lrcSize = lrcSize;
	}

}
