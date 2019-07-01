package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.CommentRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class CommentController {

    private Logger logger = Logger.getLogger(CommentController.class.getSimpleName());

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    /**
     * This method can add comment to all articles, questions and answers
     *
     * @param comment
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/addComment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Comment addComment(@RequestBody Comment comment, HttpServletRequest request) throws Exception {

        String methodName = "UserController.addComment";

        AppUser appUser = comment.getAppUser();

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            comment.setAppUser(appUser);
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
        }

        comment.setUtilTimestamp(new Date());
        return commentRepository.save(comment);
    }

    @PutMapping("/updateComment/{id}")
    @Transactional
    public ResponseEntity<Comment> updateComment(@RequestBody Comment updatedComment,
                                                 @PathVariable Long id,
                                                 HttpServletRequest request)
            throws Exception {

        String methodName = "UserController.updateComment";

        Comment oldComment = commentRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + id));

        AppUser appUser = updatedComment.getAppUser();

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            updatedComment.setAppUser(appUser);
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
        }

        // Cannot update other comment
        if (!appUser.getUserId().equals(oldComment.getAppUser().getUserId())) {
            String message = "UserController.updateComment: You cannot update others' comments";
            logger.info(message);
            throw new Exception(message);
        }

        oldComment.setContent(updatedComment.getContent());
        return ResponseEntity.ok(commentRepository.save(oldComment));
    }

    /**
     * Delete comment
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteComment/{id}")
    @Transactional
    public Map<String, String> deleteComment(@RequestBody AppUser appUser,
                                             @PathVariable Long id,
                                             HttpServletRequest request) throws Exception {

        String methodName = "UserController.deleteComment";

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new Exception(methodName + ": Found no answer with id: " + id));

        controllerUtils.validateAppUser(appUser, methodName, false);

        if (appUser.isAnonymous()) {
            appUser = controllerUtils.saveOrReturnAnonymousUser(HttpRequestResponseUtils.getClientIpAddress(request));
            comment.setAppUser(appUser);
        } else {
            controllerUtils.validateAppUser(appUser, methodName, true);
        }

        // Cannot delete other questions
        if (!appUser.getUserId().equals(comment.getAppUser().getUserId())) {
            String message = methodName + ": You cannot delete others' comment";
            logger.info(message);
            throw new Exception(message);
        }

        commentRepository.delete(comment);

        Map<String, String> map = new HashMap<>();
        map.put("commentId", "" + id);
        map.put("deleted", "true");
        return map;
    }

}
