delete from notification;
delete from article_tags;
delete from question_tags;
delete from uploaded_file;
delete from app_user_tag;
delete from edited_question_tags;
delete from report;
delete from comment_upvoted_user_ids;
delete from comment;
delete from article_upvoted_user_ids;
delete from article_subscribers;
delete from article;
delete from answer_upvoted_user_ids;
delete from answer;
delete from edited_question;
delete from question_upvoted_user_ids;
delete from question_subscribers;
delete from question;
delete from tag;
delete from app_user;
delete from social_user;

insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , '127.0.0.1', 0, 'USER', 0, null);

insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (2, 0, null , '127.0.0.1', 0, 'USER', 0, null);

insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (1, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 0, 1);

insert into notification(notification_id, seen, message, util_timestamp, user_id, article_id, question_id)
values (1, 0, 'deleted question', '2012-02-24T18:10:00',1, null , 1);
insert into notification(notification_id, seen, message, util_timestamp, user_id, article_id, question_id)
values (2, 0, 'deleted question 2', '2012-02-24T18:10:00',2, null , 1);
insert into notification(notification_id, seen, message, util_timestamp, user_id, article_id, question_id)
values (3, 0, 'deleted question 3', '2012-02-22T18:10:00',1, null , 1);
insert into notification(notification_id, seen, message, util_timestamp, user_id, article_id, question_id)
values (4, 1, 'deleted question 4', '2012-02-23T18:10:00',1, null , 1);

insert into question_subscribers(subscribed_questions_question_id, subscribers_user_id)
values (1, 1);
insert into question_subscribers(subscribed_questions_question_id, subscribers_user_id)
values (1, 2);