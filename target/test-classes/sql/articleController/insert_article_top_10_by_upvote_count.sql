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

-- Insert articles
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 1, 10);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 2, 15);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 3, 20);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 4, 110);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 5, 5);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 6, 1);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 7, 2);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 8, 50);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 9, 80);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 10, 1000);
insert into article (user_id, category, content, title, util_timestamp, view_count, article_id, upvote_count)
values (null, N'trồng trọt',N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-22T18:10:00', 0, 11, 106);