package com.son.CapstoneProject.service;

import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ViewCountingService {

    @Autowired
    private QuestionRepository questionRepository;

    // Provide thread safe map
    private Map<Long, Map<String, Long>> questionIdWithIpAddressAndTime = new ConcurrentHashMap<>();

    private static final int INCREASE_COUNT_PER_MINUTES = 15;

    private static final int MAXIMUM_TIME_SAVED_FOR_IP_IN_HOUR = 24;

    /**
     * This method is called in a separated thread to count view of the question (not in the same thread in the @RestController)
     * It doesn't matter when this method finishes its work, and users can still have the count value after receiving the question/article
     * @param questionId
     * @param ipAddress
     */
    @Async("specificTaskExecutor")
    @Transactional
    public void countView(Long questionId, String ipAddress) {
//        System.out.println("ViewCountingService.countView Delayed");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println("ViewCountingService.countView get called");
        Map<String, Long> ipAddressWithTime = questionIdWithIpAddressAndTime.get(questionId);
        Long currentTime = System.currentTimeMillis();

        // Remove the previous ip address for this question if it has been for 24 hours
        if (ipAddressWithTime != null) {
            for (Map.Entry<String, Long> entry : ipAddressWithTime.entrySet()) {
                Long timeGap = currentTime - entry.getValue(); // currentTime - previousTime
//                if (TimeUnit.MILLISECONDS.toSeconds(timeGap) >= 30) { // for test
                if (TimeUnit.MILLISECONDS.toHours(timeGap) >= MAXIMUM_TIME_SAVED_FOR_IP_IN_HOUR) {
                    ipAddressWithTime.remove(entry.getKey());
                }
            }
        }

        // First time this question is seen
        if (ipAddressWithTime == null) {
            ipAddressWithTime = new ConcurrentHashMap<>();
            ipAddressWithTime.put(ipAddress, System.currentTimeMillis());
            questionIdWithIpAddressAndTime.put(questionId, ipAddressWithTime);

            // Update to database
            updateViewCount(questionId);

        } else {
            boolean foundIp = false;
            for (Map.Entry<String, Long> entry : ipAddressWithTime.entrySet()) {
                // If this questionId contains ipAddress of an user who has viewed -> see previous time
                if (entry.getKey().equalsIgnoreCase(ipAddress)) {
                    Long previousTime = entry.getValue();
                    Long timeGap = System.currentTimeMillis() - previousTime;

                    // Update view of each user each 15 minutes
//                    if (TimeUnit.MILLISECONDS.toSeconds(timeGap) >= 10) { // for test
                    if (TimeUnit.MILLISECONDS.toMinutes(timeGap) >= INCREASE_COUNT_PER_MINUTES) {
                        // Update to database
                        updateViewCount(questionId);

                        // Remove the previous time
                        entry.setValue(currentTime);
                    }

                    foundIp = true;
                    break;
                }
            }

            // if this ipAddress hasn't come into this post
            if (!foundIp) {
                ipAddressWithTime.put(ipAddress, System.currentTimeMillis());

                // Update to database
                updateViewCount(questionId);
            }

        }
    }

    private void updateViewCount(Long questionId) {
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isPresent()) {
            Question question1 = question.get();
            question1.setViewCount(question1.getViewCount() + 1);
            questionRepository.save(question1);
        }
    }

}
