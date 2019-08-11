delete from article_tags;
delete from question_tags;
delete from uploaded_file;
delete from app_user_tag;
delete from edited_question_tags;
delete from report;
delete from comment_upvoted_user_ids;
delete from comment;
delete from article_upvoted_user_ids;
delete from article;
delete from answer_upvoted_user_ids;
delete from answer;
delete from edited_question;
delete from question_upvoted_user_ids
delete from question;
delete from tag;
delete from app_user;
delete from social_user;

-- Insert user add question
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , '127.0.0.1', 0, 'USER', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (2, 0, null , '127.0.0.2', 0, 'USER', 0, null);


-- Report for question 1 from user 2
insert into report(report_id, message, user_id, question_id)
values (1, 'message', 2, null);
insert into report(report_id, message, user_id, question_id)
values (2, 'message 2', 2, null);