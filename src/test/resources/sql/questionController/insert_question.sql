delete from question_tags where questions_question_id = 1;
delete from app_user_tag where app_user_tag_id in (0, 1);
delete from question where question_id in (1, 2, 3, 4, 5);
delete from tag where tag_id in (0, 1);
delete from app_user where user_id in (1);

-- Insert user add question
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , null, 0, 'USER', 0, null);

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