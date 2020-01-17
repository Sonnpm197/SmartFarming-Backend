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

insert into app_user (user_id, anonymous, cv_url, ip_address, reputation, role, view_count, social_id)
values (1, 0, null , '127.0.0.1', 0, 'USER', 0, null);

insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (1, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị', N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 0, 1);

insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (1, N'Câu trả lời đó là', 0, null, 1, 1);

insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (1, N'Câu bình luận đó là', null , 1, 1, null, null);