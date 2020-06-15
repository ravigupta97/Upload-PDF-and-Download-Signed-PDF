package FileUploadORDownload;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.springboot.exception.FileStorageException;
import org.springboot.exception.MyFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

	private Path fileStorageLocation;
	
	@Autowired
	 CreateSignature createSign;
	
	
	public Path getFileStorageLocation() {
		return fileStorageLocation;
	}

	public void setFileStorageLocation(Path fileStorageLocation) {
		this.fileStorageLocation = fileStorageLocation;
	}

	
	
	public FileService(FileStorageProperties fileStorageProperties) throws Exception {
	        setFileStorageLocation(Paths.get(fileStorageProperties.getUploadDir())
	                .toAbsolutePath().normalize());
	        
	        try {
	            Files.createDirectories(getFileStorageLocation());
	        } catch (Exception ex) {
	            throw new Exception("Could not create the directory where the uploaded files will be stored.", ex);
	        }
	    }
	
	public String storeFile(MultipartFile file) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		
		//createSign = new CreateSignature(keyStore, null);
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		 
		 try {
	            // Check if the file's name contains invalid characters
	            if(fileName.contains("..")) {
	                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
	            }
		
	            Path targetLocation = getFileStorageLocation().resolve(fileName);
	            createSign.SignPDF();
	            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
	            File SignedFile = createSign.SignFile(targetLocation.toFile());
	           // Path newTargetLocation = SignedFile.toPath();
	            //Files.copy(file.getInputStream(), newTargetLocation, StandardCopyOption.REPLACE_EXISTING);
	            //String newFile = StringUtils.cleanPath(((MultipartFile) SignedFile).getOriginalFilename());
	            return SignedFile.getName();
	            
	        } catch (IOException ex) {
	            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
	        }
	}
	
	public Resource loadFileAsResource(String fileName) {
		try {
            Path filePath = getFileStorageLocation().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
	

}
