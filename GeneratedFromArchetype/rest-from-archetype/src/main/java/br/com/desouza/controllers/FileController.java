package br.com.desouza.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.desouza.data.vo.v1.UploadFileResponseVO;
import br.com.desouza.services.FileStorageServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/file/v1")
@Tag(name = "File Endpoint", description = "Endpoints to manage file uploads and downloads")
public class FileController {

	@Autowired
	private FileStorageServices service;
	Logger logger = Logger.getLogger(FileController.class.getName());

	@PostMapping("/uploadFile")
	@Operation(summary = "Upload a file", description = "Upload a file", responses = {
	 	@ApiResponse(description = "Success", responseCode = "200", content = @Content),
	 	@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
	 	@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
	 	@ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content),
		}
	)
	public UploadFileResponseVO uploadFile(@RequestParam("file") MultipartFile file) {
		logger.info("Storing file to disk");
		var filename = service.storeFile(file);
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
									.path("/api/file/v1/dowloadFile/")
									.path(filename).toUriString();
		return new UploadFileResponseVO(filename,fileDownloadUri, file.getContentType(), file.getSize());
	}
	
	@PostMapping("/uploadMultipleFiles")
	@Operation(summary = "Upload files", description = "Upload files", responses = {
			@ApiResponse(description = "Success", responseCode = "200", content = @Content),
			@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
			@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			@ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content),
	}
	)
	public List<UploadFileResponseVO> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
		logger.info("Storing files to disk");
		return Arrays.asList(files).stream().map(file -> uploadFile(file)).collect(Collectors.toList());
	}
	
	@GetMapping("/dowloadFile/{filename:.+}")
	@Operation(summary = "Dowload a file", description = "Dowload a file", responses = {
	 	@ApiResponse(description = "Success", responseCode = "200", content = @Content),
	 	@ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
	 	@ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
	 	@ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content),
		}
	)
	public ResponseEntity<Resource> dowloadFile(@PathVariable String filename, HttpServletRequest request) {
		logger.info("Reading a file on disk");
		Resource resource = service.loadFileAsResource(filename);
		String contentType = "";
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (Exception e) {
			logger.info("Could not determine file type");
		}
		if(contentType.isBlank()) contentType = "application/octet-stream";
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
}