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
values (2, 0, null , '127.0.0.1', 0, 'USER', 0, 1);

-- Question id = 1 from user 1
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (1, N'Ẩm thực: đồ ăn hà nội khá chán, chắc do chỉ đa phần người Bắc nên em không hợp khẩu vị',
 N'người miền Nam sinh sống ở HN', '2012-02-24T18:10:00', 50, 1);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (2, 'content 2', 'title 2', '2012-02-25T18:10:00', 50, 1);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (3, 'content 3', 'title 3', '2012-02-26T18:10:00', 50, 1);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (4, 'content 4', 'title 4', '2012-03-1T18:10:00', 50, 1);
insert into question (question_id, content, title, util_timestamp, view_count, user_id)
values (5, 'content 5', 'title 5', '2012-03-2T18:10:00', 50, 1); -- 2/3

-- Answer id = 1 & 2 for question 1
insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (3, N'Câu trả lời đó là của người số 1 cho câu hỏi 1', 0, '2012-03-2T18:10:00', 1, 1); -- 2/3
insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (1, N'Câu trả lời đó là của người số 1 cho câu hỏi 1', 0, '2012-04-24T18:10:00', 1, 1);
insert into answer (answer_id, content, is_accepted, util_timestamp, user_id, question_id)
values (2, N'Câu trả lời đó là của người số 2 cho câu hỏi 1', 0, '2012-05-24T18:10:00', 1, 1);

-- Comment for answer id = 1
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (10, N'Câu bình luận đó là 10 cho câu trả lời số 1', '2012-03-2T18:10:00' , 1, 1, null, null); -- 2/3
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (11, N'Câu bình luận đó là 11 cho câu trả lời số 1', '2012-05-26T18:10:00' , 1, 1, null, null);
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (12, N'Câu bình luận đó là 12 cho câu trả lời số 1', '2012-05-27T18:10:00' , 1, 1, null, null);

-- Comment for question id = 1
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (13, N'Câu bình luận đó là 13 cho câu hỏi số 1', '2012-05-28T18:10:00' , null , 1, null, 1);
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (14, N'Câu bình luận đó là 14 cho câu hỏi số 1', '2012-05-29T18:10:00' , null, 1, null, 1);
insert into comment (comment_id, content, util_timestamp, answer_id, user_id, article_id, question_id)
values (15, N'Câu bình luận đó là 15 cho câu hỏi số 1', '2012-05-30T18:10:00' , null, 1, null, 1);

insert into question_upvoted_user_ids(question_question_id, upvoted_user_ids)
values (5, 2);

insert into answer_upvoted_user_ids(answer_answer_id, upvoted_user_ids)
values (3, 2);

insert into comment_upvoted_user_ids (comment_comment_id, upvoted_user_ids)
values (10, 2);