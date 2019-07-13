delete from question_tags where questions_question_id = 1;
delete from app_user_tag where app_user_tag_id in (0, 1);
delete from question where question_id in (1, 2, 3, 4, 5);
delete from tag where tag_id in (0, 1);
delete from app_user where user_id in (1);