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
delete from social_user;

insert into social_user (social_user_id, auth_token, authorization_code, email, first_name, id, id_token, last_name, name, photo_url, provider)
values (1, 'sdadsa','sadsa', 'sdadsa','sadsa', 'socialId','sadsa', 'sdadsa','sadsa', 'sdadsa','sadsa');

-- Insert user add question
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , '127.0.0.1', 0, 'USER', 0, 1);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (2, 0, null , '127.0.0.2', 0, 'ANONYMOUS', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (3, 0, null , '127.0.0.2', 0, 'ADMIN', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (4, 0, null , '127.0.0.1', 0, 'USER', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (5, 0, null , '127.0.0.1', 0, 'USER', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (6, 0, null , '127.0.0.1', 0, 'USER', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (7, 0, null , '127.0.0.1', 0, 'USER', 0, null);

-- Insert this and run all project to test search indexed tag
-- insert into tag (tag_id, description, name, reputation, view_count)
-- values (30, 'tagDescription 0',N'trồng trọt', 0, 0);
-- insert into tag (tag_id, description, name, reputation, view_count)
-- values (31, 'tagDescription 1',N'chăn nuôi', 0, 0);

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

-- App User Tag
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (0, 10, 50, 1, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (1, 10, 60, 2, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (2, 9, 40, 3, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (3, 20, 30, 4, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (4, 80, 80, 5, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (5, 10, 10, 6, 0);
insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (6, 50, 50, 7, 0);

insert into app_user_tag (app_user_tag_id, reputation, view_count, app_user_user_id, tag_tag_id)
values (10, 0, 0, 2, 1);

insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (1, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 50, 1);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, N'chăn nuôi',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-23T18:10:00', 50, 2);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, null,null, null, '2012-02-24T18:10:00', 50, 3);

insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (1, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 50, 1);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (2, 'content 2', 'title 2', '2012-02-25T18:10:00', 50, null);