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


-- Insert user add article
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , '127.0.0.1', 0, 'USER', 0, null);
insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (2, 0, null , '127.0.0.2', 0, 'USER', 0, null);

-- Insert tags
insert into tag (tag_id, description, name, reputation, view_count)
values (0, 'tagDescription0','trồng trọt', 0, 0);
insert into tag (tag_id, description, name, reputation, view_count)
values (1, 'tagDescription1','chăn nuôi', 0, 0);

-- Insert articles
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (1, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 1);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, N'chăn nuôi',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-23T18:10:00', 0, 2);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, null,null, null, '2012-02-24T18:10:00', 0, 3);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, null,null, null, '2012-02-25T18:10:00', 0, 4);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, null,null, null, '2012-02-26T18:10:00', 0, 5);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, null,null, null, '2012-02-19T18:10:00', 0, 6);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, null,null, null, '2012-02-20T18:10:00', 0, 7);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id)
values (null, null,null, null, '2012-02-21T18:10:00', 0, 8);

-- Insert into article_tags
insert into article_tags (articles_article_id, tags_tag_id) values (1, 0);
insert into article_tags (articles_article_id, tags_tag_id) values (1, 1);
