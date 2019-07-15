delete from article_tags;
delete from question_tags;
delete from uploaded_file;
delete from app_user_tag;
delete from edited_question_tags;
delete from report;
delete from comment_upvoted_user_ids;
delete from comment;
delete from article;
delete from answer;
delete from edited_question;
delete from question;
delete from tag;
delete from app_user;
delete from social_user_information;

insert into social_user_information (social_user_information_id, auth_token, authorization_code, email, first_name, id, id_token, last_name, name, photo_url, provider)
values (1, 'sdadsa','sadsa', 'sdadsa','sadsa', 'socialId','sadsa', 'sdadsa','sadsa', 'sdadsa','sadsa');

-- Insert user add question
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , '127.0.0.1', 0, 'USER', 0, 1);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (2, 0, null , '127.0.0.2', 0, 'USER', 0, null);

-- Insert tags
insert into tag (tag_id, description, name, reputation, view_count)
values (0, 'tagDescription 0',N'trồng trọt', 0, 0);
insert into tag (tag_id, description, name, reputation, view_count)
values (1, 'tagDescription 1',N'chăn nuôi', 0, 0);
-- Tag for edited questions
insert into tag (tag_id, description, name, reputation, view_count)
values (2, 'tagDescription 0 edited',N'trồng trọt edited', 0, 0);
insert into tag (tag_id, description, name, reputation, view_count)
values (3, 'tagDescription 1 edited',N'chăn nuôi edited', 0, 0);

-- This question is for test search
-- insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
-- values (10, 0, null , null, 0, 'USER', 0, null);
-- insert into question (question_id, content, title, util_timestamp, view_count, user_id)
-- values (21, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 0, 10);

-- Question id = 1 from user 1
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (1, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 0, 1);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (2, 'content 2', 'title 2', '2012-02-25T18:10:00', 0, null);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (3, 'content 3', 'title 3', '2012-02-26T18:10:00', 0, null);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (4, 'content 4', 'title 4', '2012-03-1T18:10:00', 0, null);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (5, 'content 5', 'title 5', '2012-03-2T18:10:00', 0, null);

-- Insert into article_tags
insert into question_tags (questions_question_id, tags_tag_id) values (1, 0);
insert into question_tags (questions_question_id, tags_tag_id) values (1, 1);

-- App User Tag
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (0, 0, 0, 1, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (1, 0, 0, 1, 1);

-- Answer id = 1 & 2 for question 1
insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (1, N'Câu trả lời đó là của người số 1 cho câu hỏi 1', 0, null, 1, 1);
insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (2, N'Câu trả lời đó là của người số 2 cho câu hỏi 1', 0, null, 2, 1);

-- Comment for answer id = 1
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (10, N'Câu bình luận đó là 10 cho câu trả lời số 1', null , 1, 1, null, null);
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (11, N'Câu bình luận đó là 11 cho câu trả lời số 1', null , 1, 1, null, null);
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (12, N'Câu bình luận đó là 12 cho câu trả lời số 1', null , 1, 1, null, null);

-- Comment for question id = 1
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (13, N'Câu bình luận đó là 10 cho câu hỏi số 1', null , null , 1, null, 1);
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (14, N'Câu bình luận đó là 11 cho câu hỏi số 1', null , null, 2, null, 1);
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (15, N'Câu bình luận đó là 12 cho câu hỏi số 1', null , null, 1, null, 1);

-- Uploaded file for article 1
insert into uploaded_file (id, bucket_name, uploaded_file_name, uploaded_file_url_shown_onui, article_id, question_id)
values (1, 'bucket_name', 'uploaded_file_name', 'uploaded_file_url_shown_onui', 1, null);
insert into uploaded_file (id, bucket_name, uploaded_file_name, uploaded_file_url_shown_onui, article_id, question_id)
values (2, 'bucket_name', 'uploaded_file_name 2 ', 'uploaded_file_url_shown_onui 2', 1, null);

-- Updated uploaded file for article 1 (article_id = null because at this moment these 2 are not assigned to article1 yet)
insert into uploaded_file (id, bucket_name, uploaded_file_name, uploaded_file_url_shown_onui, article_id, question_id)
values (3, 'bucket_name', 'updated_uploaded_file_name', 'uploaded_file_url_shown_onui', null, null);
insert into uploaded_file (id, bucket_name, uploaded_file_name, uploaded_file_url_shown_onui, article_id, question_id)
values (4, 'bucket_name', 'updated_uploaded_file_name_2 ', 'uploaded_file_url_shown_onui_2', null, null);

-- Report for question 1 from user 2
insert into report(report_id, message, user_id, question_id)
values (1, 'message', 2, 1);
insert into report(report_id, message, user_id, question_id)
values (2, 'message 2', 2, 1);

-- EditedQuestion for question 1 from user 2
insert into edited_question(edited_question_id, content, title, util_timestamp, user_id, question_id)
values (1, 'edited_question', 'title', null, 2, 1);
insert into edited_question(edited_question_id, content, title, util_timestamp, user_id, question_id)
values (2, 'edited_question version 2', 'title', null, 2, 1);

-- Edited question from user 2 has 2 tags
insert into edited_question_tags (edited_question_edited_question_id, tags_tag_id) values (1, 2);
insert into edited_question_tags (edited_question_edited_question_id, tags_tag_id) values (1, 3);