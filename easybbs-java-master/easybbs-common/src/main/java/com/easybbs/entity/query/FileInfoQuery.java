package com.easybbs.entity.query;



/**
 * 
 * 文件信息参数
 * 
 */
public class FileInfoQuery extends BaseParam {


	/**
	 * 文件ID
	 */
	private Integer fileId;

	/**
	 * 文章ID
	 */
	private String articleId;

	private String articleIdFuzzy;

	/**
	 * 文件大小
	 */
	private Long fileSize;

	/**
	 * 文件名称
	 */
	private String fileName;

	private String fileNameFuzzy;

	/**
	 * 下载次数
	 */
	private Integer downloadCount;

	/**
	 * 文件路径
	 */
	private String filePath;

	private String filePathFuzzy;

	/**
	 * 文件类型
	 */
	private Integer fileType;


	public void setFileId(Integer fileId){
		this.fileId = fileId;
	}

	public Integer getFileId(){
		return this.fileId;
	}

	public void setArticleId(String articleId){
		this.articleId = articleId;
	}

	public String getArticleId(){
		return this.articleId;
	}

	public void setArticleIdFuzzy(String articleIdFuzzy){
		this.articleIdFuzzy = articleIdFuzzy;
	}

	public String getArticleIdFuzzy(){
		return this.articleIdFuzzy;
	}

	public void setFileSize(Long fileSize){
		this.fileSize = fileSize;
	}

	public Long getFileSize(){
		return this.fileSize;
	}

	public void setFileName(String fileName){
		this.fileName = fileName;
	}

	public String getFileName(){
		return this.fileName;
	}

	public void setFileNameFuzzy(String fileNameFuzzy){
		this.fileNameFuzzy = fileNameFuzzy;
	}

	public String getFileNameFuzzy(){
		return this.fileNameFuzzy;
	}

	public void setDownloadCount(Integer downloadCount){
		this.downloadCount = downloadCount;
	}

	public Integer getDownloadCount(){
		return this.downloadCount;
	}

	public void setFilePath(String filePath){
		this.filePath = filePath;
	}

	public String getFilePath(){
		return this.filePath;
	}

	public void setFilePathFuzzy(String filePathFuzzy){
		this.filePathFuzzy = filePathFuzzy;
	}

	public String getFilePathFuzzy(){
		return this.filePathFuzzy;
	}

	public void setFileType(Integer fileType){
		this.fileType = fileType;
	}

	public Integer getFileType(){
		return this.fileType;
	}

}
