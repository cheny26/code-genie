-- 创建库
create database if not exists `code-genie`;

-- 切换库
use `code-genie`;

-- 用户表
DROP TABLE IF EXISTS `user`;
create table `user`
(
    id           bigint auto_increment comment 'id'      primary key,
    user_name     varchar(256)                           null comment '用户名',
    user_password varchar(256)                           not null comment '密码',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    email         varchar(50)  default ''                null comment '用户邮箱',
    user_role     varchar(64)  default 'user'            not null comment '用户角色：user/admin',
    points        int          default 100               not null comment '用户积分',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_user_account (user_name)
) comment '用户' collate = utf8mb4_unicode_ci;
