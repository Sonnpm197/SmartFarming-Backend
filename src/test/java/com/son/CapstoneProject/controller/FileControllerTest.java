package com.son.CapstoneProject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.UploadedFile;
import com.son.CapstoneProject.repository.UploadedFileRepository;
import com.son.CapstoneProject.service.googleStorage.BlobHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    private UploadedFile uploadedFile(MockMultipartFile multipartFile) throws Exception {

        String url = createURL(8080, "/file/uploadFile");

        MvcResult result = this.mvc.perform(fileUpload(url).file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        String uploadedJson = result.getResponse().getContentAsString();
        System.out.println(">> Result: " + uploadedJson);

        UploadedFile uploadedFileResponse = new ObjectMapper().readValue(uploadedJson, UploadedFile.class);

        // Delete from cloud
        BlobHandler.getInstance().deleteBlob(uploadedFileResponse.getBucketName(), uploadedFileResponse.getUploadedFileName());

        return uploadedFileRepository.findByUploadedFileName(uploadedFileResponse.getUploadedFileName());
    }

    @Test
    @Sql(scripts = "/sql/fileController/clearUploadedFile.sql", executionPhase = AFTER_TEST_METHOD)
    public void uploadImageFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "Capture1.PNG",
                "text/plain", Files.readAllBytes(Paths.get("C:\\IntelliJ Projects\\CapstoneProject\\src\\test\\resources\\file\\Capture1.PNG")));
        UploadedFile uploadedFile = uploadedFile(multipartFile);
        // Save to DB
        Assert.assertNotNull(uploadedFile);
    }

    @Test
    @Sql(scripts = "/sql/fileController/clearUploadedFile.sql", executionPhase = AFTER_TEST_METHOD)
    public void uploadPDFFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "sample.pdf",
                "text/plain", Files.readAllBytes(Paths.get("C:\\IntelliJ Projects\\CapstoneProject\\src\\test\\resources\\file\\sample.pdf")));

        UploadedFile uploadedFile = uploadedFile(multipartFile);
        // Save to DB
        Assert.assertNotNull(uploadedFile);
    }

    @Test
    @Sql(scripts = "/sql/fileController/clearUploadedFile.sql", executionPhase = AFTER_TEST_METHOD)
    public void uploadMSWordFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "word.docx",
                "text/plain", Files.readAllBytes(Paths.get("C:\\IntelliJ Projects\\CapstoneProject\\src\\test\\resources\\file\\word.docx")));

        UploadedFile uploadedFile = uploadedFile(multipartFile);
        // Save to DB
        Assert.assertNotNull(uploadedFile);
    }

    /**
     * TODO: change root method FileControllers.updateFile to PostMapping
     *
     * @throws Exception
     */
    @Test
    @Sql(scripts = "/sql/fileController/clearUploadedFile.sql", executionPhase = AFTER_TEST_METHOD)
    public void updateFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "word.docx",
                "text/plain", Files.readAllBytes(Paths.get("C:\\IntelliJ Projects\\CapstoneProject\\src\\test\\resources\\file\\word.docx")));

        String url = createURL(8080, "/file/uploadFile");

        MvcResult result = this.mvc.perform(fileUpload(url).file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        String uploadedJson = result.getResponse().getContentAsString();
        System.out.println(">> Result from upload: " + uploadedJson);

        UploadedFile uploadedFileResponse = new ObjectMapper().readValue(uploadedJson, UploadedFile.class);

        multipartFile = new MockMultipartFile(
                "file", "newword.docx",
                "text/plain", Files.readAllBytes(Paths.get("C:\\IntelliJ Projects\\CapstoneProject\\src\\test\\resources\\file\\newword.docx")));

        // Update file which has been uploaded
        result = this.mvc.perform(fileUpload(createURL(8080, "/file/updateFile") + "/" + uploadedFileResponse.getId())
                .file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        uploadedJson = result.getResponse().getContentAsString();
        System.out.println(">> Result from upload: " + uploadedJson);

        uploadedFileResponse = new ObjectMapper().readValue(uploadedJson, UploadedFile.class);

        // Delete from cloud
        BlobHandler.getInstance().deleteBlob(uploadedFileResponse.getBucketName(), uploadedFileResponse.getUploadedFileName());

        UploadedFile uploadedFileSavedToDB = uploadedFileRepository.findByUploadedFileName(uploadedFileResponse.getUploadedFileName());

        Assert.assertNotNull(uploadedFileSavedToDB);
    }

    /**
     * TODO: change root method FileControllers.deleteFile to PostMapping
     *
     * @throws Exception
     */
    @Test
    @Sql(scripts = "/sql/fileController/clearUploadedFile.sql", executionPhase = AFTER_TEST_METHOD)
    public void deleteFile() throws Exception {
        // Upload file to delete
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "word.docx",
                "text/plain", Files.readAllBytes(Paths.get("C:\\IntelliJ Projects\\CapstoneProject\\src\\test\\resources\\file\\word.docx")));

        String url = createURL(8080, "/file/uploadFile");

        MvcResult result = this.mvc.perform(fileUpload(url).file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        String uploadedJson = result.getResponse().getContentAsString();
        System.out.println(">> Result from upload: " + uploadedJson);

        UploadedFile uploadedFileResponse = new ObjectMapper().readValue(uploadedJson, UploadedFile.class);

        // Then delete

        url = createURL(8080, "/file/deleteFile");

        String bucketName = uploadedFileResponse.getBucketName();
        String uploadedFileName = uploadedFileResponse.getUploadedFileName();

        String requestBody = "{"
                + "\"bucketName\" : " + "\"" + bucketName + "\","
                + "\"uploadedFileName\" : " + "\"" + uploadedFileName + "\""
                + "}";

        result = this.mvc.perform(delete(url)
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestBody))
                .andReturn();

        uploadedJson = result.getResponse().getContentAsString();
        System.out.println(">> Result from upload: " + uploadedJson);

        // Assert from cloud
        Blob blob = BlobHandler.getInstance().getBlobFromId(bucketName, uploadedFileName);
        Assert.assertNull(blob);

        // Assert from DB
        UploadedFile uploadedFileFromDB = uploadedFileRepository.findByBucketNameAndUploadedFileName(uploadedFileName, uploadedFileName);
        Assert.assertNull(uploadedFileFromDB);

    }
}