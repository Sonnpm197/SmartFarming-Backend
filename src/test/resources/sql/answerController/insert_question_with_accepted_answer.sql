delete from article_tags;
delete from question_tags;
delete from uploaded_file;
delete from app_user_tag;
delete from comment_upvoted_user_ids;
delete from comment;
delete from article;
delete from answer;
delete from question;
delete from tag;
delete from app_user;

-- Insert user add question
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , '127.0.0.1', 0, 'USER', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (2, 0, null , '127.0.0.2', 0, 'USER', 0, null);

-- Insert tags
insert into tag (tag_id, description, name, reputation, view_count)
values (0, 'tagDescription 0','trồng trọt', 0, 0);
insert into tag (tag_id, description, name, reputation, view_count)
values (1, 'tagDescription 1','chăn nuôi', 0, 0);

-- This question is for test search
-- insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
-- values (10, 0, null , null, 0, 'USER', 0, null);
-- insert into question (question_id, content, title, util_timestamp, view_count, user_id)
-- values (21, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 0, 10);

insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (1, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 0, 1);
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

-- User 2 has 1 accepted answer in a question
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (2, 1, 0, 2, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (3, 1, 0, 2, 1);
-----------------------------------

insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (1, N'Câu trả lời đó là của người số 1 cho câu hỏi 1', 0, null, 1, 1);

insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (2, N'Câu trả lời đó là của người số 2 cho câu hỏi 1', 1, null, 2, 1);

insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (10, N'Câu bình luận đó là 10', null , 1, 1, null, null);

insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (11, N'Câu bình luận đó là 11', null , 1, 1, null, null);

insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (12, N'Câu bình luận đó là 12', null , 1, 1, null, null);