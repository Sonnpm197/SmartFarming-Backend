package com.son.CapstoneProject.controller;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.entity.UploadedFile;
import com.son.CapstoneProject.repository.UploadedFileRepository;
import com.son.CapstoneProject.service.googleStorage.BlobHandler;
import net.sf.jmimemagic.Magic;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.son.CapstoneProject.common.ConstantValue.GOOGLE_ACCESS_FILE_PREFIX_URL;

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class FileController {

//    @Autowired
//    private FileStorageService fileStorageService;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @PostMapping("/uploadFile")
    public UploadedFile uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
//        String fileName = fileStorageService.storeFile(file);
//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/downloadFile/")
//                .path(fileName)
//                .toUriString();
//
//        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());

        String methodName = "FileController.uploadFile: ";

        validateFile(file, methodName);

        String bucketName = getBucketNameByContentType(file);

        if (bucketName.equals(ConstantValue.UNKNOWN_FILE_BUCKET)) {
            throw new Exception(methodName + "File type: " + file.getContentType() + " is not supported");
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Upload file using google cloud storage
        try {
            Blob blob = BlobHandler.getInstance().createBlobFromByteArray(bucketName, fileName, file.getBytes(), file.getContentType());
            BlobId blobId = blob.getBlobId();
            String uploadedBucketName = blobId.getBucket();
            String uploadedFileName = blobId.getName();

            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setBucketName(uploadedBucketName);
            uploadedFile.setUploadedFileName(uploadedFileName);
            uploadedFile.setUploadedFileUrlShownOnUI(GOOGLE_ACCESS_FILE_PREFIX_URL + "/" + uploadedBucketName + "/" + uploadedFileName);

            // Save to UploadedFile table
            uploadedFileRepository.save(uploadedFile);

            return uploadedFile;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return list of uploaded files which can be viewed on browsers
     *
     * @param files
     * @return
     */
    @PostMapping("/uploadMultipleFiles")
    public List<UploadedFile> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) throws Exception {
        List<UploadedFile> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedFiles.add(uploadFile(file));
        }

        return uploadedFiles;
    }

    /**
     * This method is to update file on UI
     * It needs fileName on UI (without the full link) to find the blob on gg cloud to delete
     *
     * @return
     * @throws Exception
     */
    @PutMapping("/updateFile/{uploadedFileId}")
    public UploadedFile updateFile(@RequestParam("file") MultipartFile updatedFile, @PathVariable Long uploadedFileId) throws Exception {

        String methodName = "FileController.changeFile: ";

        validateFile(updatedFile, methodName);

        Optional<UploadedFile> uploadedFileOnUIOptional = uploadedFileRepository.findById(uploadedFileId);

        if (!uploadedFileOnUIOptional.isPresent()) {
            return null;
        }

        UploadedFile uploadedFileOnUI = uploadedFileOnUIOptional.get();

        String bucketName = uploadedFileOnUI.getBucketName();

        String fileName = uploadedFileOnUI.getUploadedFileName();

        // Upload file using google cloud storage
        try {
            Blob blob = BlobHandler.getInstance().updateBlob(bucketName, fileName, updatedFile.getBytes(), updatedFile.getContentType());
            BlobId blobId = blob.getBlobId();
            String uploadedBucketName = blobId.getBucket();
            String uploadedFileName = blobId.getName();

            UploadedFile uploadedFile = new UploadedFile();
            uploadedFile.setBucketName(bucketName);
            uploadedFile.setUploadedFileName(uploadedFileName);
            uploadedFile.setUploadedFileUrlShownOnUI(GOOGLE_ACCESS_FILE_PREFIX_URL + "/" + uploadedBucketName + "/" + uploadedFileName);

            // Save to UploadedFile table
            uploadedFileRepository.save(uploadedFile);

            return uploadedFile;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestBody UploadedFile uploadedFileOnUI) {

        String bucketName = uploadedFileOnUI.getBucketName();

        String fileName = uploadedFileOnUI.getUploadedFileName();

        // Upload file using google cloud storage
        try {
            boolean deleted = BlobHandler.getInstance().deleteBlob(bucketName, fileName);

            return (deleted ? "successfully" : "failed") + " deleted file url: " + GOOGLE_ACCESS_FILE_PREFIX_URL + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method is to download a file from URI
     * *Note: to use regular expression we need a format like: varName:regex
     * @return
     */
//    @GetMapping("/downloadFile/{fileName:.+}")
//    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
//        // Load file as Resource
//        Resource resource = fileStorageService.loadFileAsResource(fileName);
//
//        // Try to determine file's content type
//        String contentType = null;
//        try {
//            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        // Fallback to the default content type if type could not be determined
//        if (contentType == null) {
//            contentType = "application/octet-stream";
//        }
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }
    private String getBucketNameByContentType(MultipartFile file) throws Exception {
        byte[] input = file.getBytes();
        if (isImage(input)) {
            return ConstantValue.FILE_IMAGE_BUCKET;
        } else if (isPDF(input)) {
            return ConstantValue.FILE_PDF_BUCKET;
        } else if (isMSWord(input)) {
            return ConstantValue.FILE_WORD_BUCKET;
        }

        return ConstantValue.UNKNOWN_FILE_BUCKET;
    }

    private void validateFile(MultipartFile file, String methodName) throws Exception {
        if (file == null) {
            throw new Exception(methodName + "File not found");
        }

        if (file.getOriginalFilename() == null) {
            throw new Exception(methodName + "File name is null");
        }
    }

    private boolean isImage(byte[] array) {
        try {
            String mimeType = Magic.getMagicMatch(array, false).getMimeType();
            if (mimeType.startsWith("image/")) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean isPDF(byte[] array) {
        try {
            PDDocument.load(array);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isMSWord(byte[] array) {
        try {
            XWPFDocument xdoc = new XWPFDocument(new ByteArrayInputStream(array));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
