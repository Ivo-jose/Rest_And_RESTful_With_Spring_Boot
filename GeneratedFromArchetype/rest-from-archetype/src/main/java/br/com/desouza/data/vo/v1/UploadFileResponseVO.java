package br.com.desouza.data.vo.v1;

import java.io.Serializable;

public class UploadFileResponseVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String filename;
	private String fileDownloadUri;
	private String fileType;
	private long size;
	
	public UploadFileResponseVO() {}
	
	public UploadFileResponseVO(String filename, String fileDownloadUri, String fileType, long size) {
		this.filename = filename;
		this.fileDownloadUri = fileDownloadUri;
		this.fileType = fileType;
		this.size = size;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFileDownloadUri() {
		return fileDownloadUri;
	}

	public void setFileDownloadUri(String fileDownloadUri) {
		this.fileDownloadUri = fileDownloadUri;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
