-- 新增表
DROP TABLE TATEMPLATE cascade constraints;
CREATE TABLE TATEMPLATE (
    TEMPLATEID      VARCHAR2(36)     NOT NULL,
    TEMPLATENAME    VARCHAR2(100)    NOT NULL,
    TEMPLATETYPE    VARCHAR2(2)      NOT NULL,
    TEMPLATEINTRO   NVARCHAR2(255)   NOT NULL,
    TEMPLATECONTENT CLOB             NOT NULL,
    EFFECTIVE       VARCHAR2(1)      NOT NULL,
    DESTROY         VARCHAR2(1)      NOT NULL,
    CREATEUSER      VARCHAR2(36)     NOT NULL,
    CREATEDATE      DATE             NOT NULL,
    constraint PK_TATEMPLATE primary key (TEMPLATEID)
);
COMMENT ON TABLE TATEMPLATE IS '在线表单模板表';
COMMENT ON COLUMN TATEMPLATE.TEMPLATEID IS '模板ID';
COMMENT ON COLUMN TATEMPLATE.TEMPLATENAME IS '模板名称';
COMMENT ON COLUMN TATEMPLATE.TEMPLATETYPE IS '模板类型';
COMMENT ON COLUMN TATEMPLATE.TEMPLATEINTRO IS '模板描述';
COMMENT ON COLUMN TATEMPLATE.TEMPLATECONTENT IS '模板内容';
COMMENT ON COLUMN TATEMPLATE.EFFECTIVE IS '有效性';
COMMENT ON COLUMN TATEMPLATE.DESTROY IS '是否销毁';
COMMENT ON COLUMN TATEMPLATE.CREATEUSER IS '创建者';
COMMENT ON COLUMN TATEMPLATE.CREATEDATE IS '创建时间';

-- 新增url数据
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('253c2eae3e0046c2ba4f53f4ffa89ba3', '在线表单模板管理', 'onlineForm/templateMg/templateMgRestService/**', NULL, '0', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('d9a5e31b83b141279b387bc4dfac0713', '表单模板条件分页查询', 'onlineForm/templateMg/templateMgRestService/queryTemplate', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('66ee423b80f84407a952e4edf2d27901', '通过ID查询模板JSON字符串', 'onlineForm/templateMg/templateMgRestService/queryTemplateContentById', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('2133136c1fca4fa79ed4c5b960bf914c', '新增表单模板', 'onlineForm/templateMg/templateMgRestService/insertTemplate', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('8d50ff41b6aa42c383f119cc4dd9abf6', '更新表单模板', 'onlineForm/templateMg/templateMgRestService/updateTemplate', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('59ea5a521dd1439f839466603c76302a', '更新模板有效性', 'onlineForm/templateMg/templateMgRestService/updateTemplateEffective', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('c433ed5a2a1340f6a0063637861de279', '删除表单模板', 'onlineForm/templateMg/templateMgRestService/deleteTemplates', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('632895cc5cd14dbdb76d9c10fffd0786', '获取表结构', 'onlineForm/templateMg/templateMgRestService/queryTableColumns', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
INSERT INTO taurl (id, name, url, namespace, type, effective, createtime, createuser) VALUES ('60bd58fe73d0472881349ecbfdd0bd29', '获取数据库表', 'onlineForm/templateMg/templateMgRestService/queryTable', '253c2eae3e0046c2ba4f53f4ffa89ba3', '1', '1', sysdate, '1');
-- 新增resource数据
INSERT INTO taresource(resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image) VALUES ('823c2134cae14d029f7db31e42b3451f', '7459c1b525934151a1d309a304933644', '在线表单模板管理', NULL, 'sysmg', 'onlineForm.html#/formTemplate', '70', '40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/7459c1b525934151a1d309a304933644/823c2134cae14d029f7db31e42b3451f', '银海软件/管理系统/资源管理/在线表单模板管理', '3', '', '', '2', 0, '1', '1', '1', NULL, sysdate, '1', '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL);
-- 新增resourceurl数据
INSERT INTO taresourceurl (resourceid, urlid, authoritypolicy) VALUES ('823c2134cae14d029f7db31e42b3451f', '253c2eae3e0046c2ba4f53f4ffa89ba3', '0');

-- 调整tadict字段长度
ALTER TABLE TADICT MODIFY  CSSCLASS varchar2(128);
-- 删除dict数据
DELETE FROM TADICT WHERE TYPE = 'TEMPLATESOURCE';
DELETE FROM TADICT WHERE type = 'TEMPLATETYPE' AND VALUE in ('2', '3');
-- 更新dict数据
UPDATE TADICT SET LABEL = '表单模板' WHERE TYPE = 'TEMPLATETYPE' AND VALUE = '1'
-- 添加dict数据
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('模版类型', 'TEMPLATETYPE', '开发模板', '2', NULL, 20, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('配置数据类型', 'CONFIGDATATYPE', 'YAML(YML)', '5', NULL, 50, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

-- tasysconfig字段
ALTER TABLE tasysconfig ADD originalvalue varchar2(800);
comment on column TASYSCONFIG.originalvalue is '配置原始值';

-- 新增tauserresource表
create table tauserresource
(
    userid     VARCHAR2(36) not null,
    resourceid VARCHAR2(36) not null,
    createtime timestamp   not null,
    constraint PK_tauserresource primary key (userid, resourceid)
);
comment on table tauserresource is '用户资源表';
comment on column tauserresource.userid is '用户id';
comment on column tauserresource.resourceid is '资源id';
comment on column tauserresource.createtime is '创建时间';

-- 添加taaccesssystem扩展字段
ALTER TABLE taaccesssystem ADD field01 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field02 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field03 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field04 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field05 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field06 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field07 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field08 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field09 VARCHAR2(1000);
ALTER TABLE taaccesssystem ADD field10 VARCHAR2(1000);
comment on column TAACCESSSYSTEM.FIELD01 is '扩展字段01';
comment on column TAACCESSSYSTEM.FIELD02 is '扩展字段02';
comment on column TAACCESSSYSTEM.FIELD03 is '扩展字段03';
comment on column TAACCESSSYSTEM.FIELD04 is '扩展字段04';
comment on column TAACCESSSYSTEM.FIELD05 is '扩展字段05';
comment on column TAACCESSSYSTEM.FIELD06 is '扩展字段06';
comment on column TAACCESSSYSTEM.FIELD07 is '扩展字段07';
comment on column TAACCESSSYSTEM.FIELD08 is '扩展字段08';
comment on column TAACCESSSYSTEM.FIELD09 is '扩展字段09';
comment on column TAACCESSSYSTEM.FIELD10 is '扩展字段10';


-- 配置菜单图标
UPDATE taresource SET icon='setting' WHERE resourceid = '0415d44401b24605a21b589b6aaa349e';
UPDATE taresource SET icon='layout' WHERE resourceid = '0aac95c1e73947bea41be639cc4e9036';
UPDATE taresource SET icon='setting' WHERE resourceid = '1d4e283ad5584e02811f6b188d3592bc';
UPDATE taresource SET icon='cluster' WHERE resourceid = '1e706f26bc144c1da12022359c238053';
UPDATE taresource SET icon='hdd' WHERE resourceid = '322e200d71544e3986d2f374e3506805';
UPDATE taresource SET icon='deployment-unit' WHERE resourceid = '3dbde33722154503a7d22ac60f6a0e4e';
UPDATE taresource SET icon='tags' WHERE resourceid = '3df588fc565d4287b3cefcd00a39cd91';
UPDATE taresource SET icon='exception' WHERE resourceid = '43f468b40c6c4c76a3a2fe4be903f4c7';
UPDATE taresource SET icon='usergroup-add' WHERE resourceid = '48afedddc8f04c668b3c1572c30a7745';
UPDATE taresource SET icon='gold' WHERE resourceid = '4b2eee0d7ded4e8094d4acf439fd3a1c';
UPDATE taresource SET icon='global' WHERE resourceid = '5611d1533d494a839c0be1e7a05da31f';
UPDATE taresource SET icon='api' WHERE resourceid = '59a7fb9c459a4dd48d468f2add1d32b2';
UPDATE taresource SET icon='block' WHERE resourceid = '5e67c7acef914c349d8aff076921f6b5';
UPDATE taresource SET icon='block' WHERE resourceid = '60554e93387146bb9c7357907ba093fa';
UPDATE taresource SET icon='form' WHERE resourceid = '722e1cc774a14178a488eb42ef4099de';
UPDATE taresource SET icon='form' WHERE resourceid = '72888507aba5484a8942e8dd0e6b6f7f';
UPDATE taresource SET icon='align-left' WHERE resourceid = '7459c1b525934151a1d309a304933644';
UPDATE taresource SET icon='safety-certificate' WHERE resourceid = '78ad02fdb879406ebc5e7a4faf8f5905';
UPDATE taresource SET icon='laptop' WHERE resourceid = '7b7f9cd1675a4b54b05c7c7cf0a7ac63';
UPDATE taresource SET icon='file-sync' WHERE resourceid = '7c1dabd160974d8f90858c187cefa128';
UPDATE taresource SET icon='snippets' WHERE resourceid = '823c2134cae14d029f7db31e42b3451f';
UPDATE taresource SET icon='team' WHERE resourceid = '877e407281dd48acb05a77fcb922bc73';
UPDATE taresource SET icon='safety' WHERE resourceid = '8aa86ed4c7f84183935a262db4a605d3';
UPDATE taresource SET icon='idcard' WHERE resourceid = '95bb9b749bf54e4692b0b1f14fd1b5ab';
UPDATE taresource SET icon='database' WHERE resourceid = 'a3c94b4edf1e4e9d8665a81dc1c5f778';
UPDATE taresource SET icon='user' WHERE resourceid = 'bd9d0bba145c458e841aa9da0aeeb1d8';
UPDATE taresource SET icon='compass' WHERE resourceid = 'bf447212de284c79a0d73c658d0692b4';
UPDATE taresource SET icon='copyright' WHERE resourceid = 'c2745b7cae7846acb9bcf8d0f4e836e8';
UPDATE taresource SET icon='clock-circle' WHERE resourceid = 'c578d9f8626d48f2971d7a18ac5281c5';
UPDATE taresource SET icon='tool' WHERE resourceid = 'cd49aa1e1a724404a4dfb4f290e1ed62';
UPDATE taresource SET icon='user' WHERE resourceid = 'daceeff8a97b46cb9573b93ba3a5a792';
UPDATE taresource SET icon='compass' WHERE resourceid = 'ec56a0a43b09429482632cb61f7c6908';
UPDATE taresource SET icon='gold' WHERE resourceid = 'fb8637c2e52e4b05bd2c07d742141ee7';
UPDATE taresource SET icon='form' WHERE resourceid = 'fe8be18859b5478d8b76a7653f02e5eb';
UPDATE taresource SET icon='book' WHERE resourceid = 'ffa74f43e853441dac0ee90c787cb2e6';

-- 菜单变动
-- INSERT INTO taresource(resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image) VALUES ('823c2134cae14d029f7db31e42b3451f', '7459c1b525934151a1d309a304933644', '在线表单模板管理', NULL, 'sysmg', 'onlineForm.html#/formTemplate', '70', '40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/7459c1b525934151a1d309a304933644/823c2134cae14d029f7db31e42b3451f', '银海软件/管理系统/资源管理/在线表单模板管理', '3', '', '', '2', 0, '1', '1', '1', NULL, sysdate, '1', '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL);
-- 删除在线表单模板管理菜单
DELETE FROM taresource WHERE resourceid = '823c2134cae14d029f7db31e42b3451f';
-- 变更润乾模板管理菜单前端路径
UPDATE taresource SET url = 'functionModules.html#/runqian' WHERE resourceid = '722e1cc774a14178a488eb42ef4099de';

-- 学历码表
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('学历', 'EDUCATION', '初中及以下', '1', NULL, 10, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('学历', 'EDUCATION', '中专/中技', '2', NULL, 20, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('学历', 'EDUCATION', '高中', '3', NULL, 30, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('学历', 'EDUCATION', '大专', '4', NULL, 40, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('学历', 'EDUCATION', '本科', '5', NULL, 50, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('学历', 'EDUCATION', '硕士', '6', NULL, 60, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createdate, createuser, version, status, field01, field02, field03, field04, field05, system, newtype) VALUES ('学历', 'EDUCATION', '博士', '7', NULL, 70, '0', NULL, NULL, NULL, sysdate, '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

-- 调整url独立授权表的主键
ALTER TABLE taroleurlauthority DROP CONSTRAINT PK_taroleurlauthority;
ALTER TABLE taroleurlauthority ADD constraint PK_taroleurlauthority primary key (roleid, urlid, resourceid)

-- 5.3.3.RELEASE 添加导入导出功能及注册用户权限的url数据
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('c92a08d19d514b6c8c462de3174b16a9','注册角色权限管理','org/authority/registeredRoleAuthorityManagementRestService/**',NULL,'0','1','2023-01-13 15:50:16','1');
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('12f03962496b4bb496f1932a1a93f92c','下载人员导入模板','org/orguser/userManagementRestService/downloadUserFIle','b32b96d0142d4fea8d5fd8a58011c0c2','1','1','2023-01-18 15:47:29','1');
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('6b8628679fd84bf989030e328248bd0a','下载组织导入模板','org/orguser/orgManagementRestService/downloadOrgFIle','aa60a746c0a24640a06dea49a4d42572','1','1','2023-01-18 15:48:18','1');
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('b117d8dfb50946bfb8714fcb235845d2','下载功能资源导入模板','org/sysmg/resourceManagementRestService/downloadResourceFIle','821a8ca012154ba2a4c451918c99cfc0','1','1','2023-01-18 15:48:56','1');
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('a2fe720ce245464ea4cf8997645d8ae4','导入导出功能','org/orguser/importAndExportService/**',NULL,'0','1','2023-01-29 16:57:44','1');

-- 5.3.3.RELEASE 新增导入导出功能及注册用户权限resource数据
INSERT INTO taresource (resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image) VALUES('9ac0beaa436d4b5f84a1af960b1f051a','6e0ea4bd764e41c4858ca4e6688f6a34','功能资源导入导出','','sysmg','sysmg.html#/resourceImportAndExport','60.000000000000000000000000000000','40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/6e0ea4bd764e41c4858ca4e6688f6a34/9ac0beaa436d4b5f84a1af960b1f051a','银海软件/管理系统/导入导出界面/功能资源导入导出','3','snippets',NULL,'2','2.000000000000000000000000000000','1','1','1',NULL,'2022-10-24 17:09:53','1','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL);
INSERT INTO taresource (resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image) VALUES('a736fdf7bd6c46c39b489094a9d8b77f','6e0ea4bd764e41c4858ca4e6688f6a34','组织人员导入导出','','sysmg','orguser.html#/importAndExport','10.000000000000000000000000000000','40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/6e0ea4bd764e41c4858ca4e6688f6a34/a736fdf7bd6c46c39b489094a9d8b77f','银海软件/管理系统/导入导出界面/组织人员导入导出','3','team',NULL,'2','2.000000000000000000000000000000','1','1','1',NULL,'2022-10-20 16:23:02','1','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL);
INSERT INTO taresource (resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image) VALUES('4d2f1971a6d947898a848aa775a45b0c','78ad02fdb879406ebc5e7a4faf8f5905','注册用户权限管理','','sysmg','authority.html#/registerAuthorityManagement','40.000000000000000000000000000000','40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/78ad02fdb879406ebc5e7a4faf8f5905/4d2f1971a6d947898a848aa775a45b0c','银海软件/管理系统/资源权限管理/注册用户权限管理','3','usergroup-add',NULL,'2','2.000000000000000000000000000000','1','1','1',NULL,'2022-12-05 03:28:40','1','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL);
INSERT INTO taresource (resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image) VALUES('6e0ea4bd764e41c4858ca4e6688f6a34','7459c1b525934151a1d309a304933644','导入导出界面','','sysmg','','90.000000000000000000000000000000','40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/7459c1b525934151a1d309a304933644/6e0ea4bd764e41c4858ca4e6688f6a34','银海软件/管理系统/资源管理/导入导出界面','2','bars',NULL,'2','2.000000000000000000000000000000','1','1','1',NULL,'2022-10-20 16:22:19','1','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL);

-- 5.3.3.RELEASE 新增导入导出功能注册用户权限对应resourceurl数据
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('9ac0beaa436d4b5f84a1af960b1f051a','5222eb06a59346d1a0b9e7906ee68a3b','0');
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('9ac0beaa436d4b5f84a1af960b1f051a','819ae767e40440a690d2f9093b879232','0');
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('a736fdf7bd6c46c39b489094a9d8b77f','a2fe720ce245464ea4cf8997645d8ae4','0');
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('9ac0beaa436d4b5f84a1af960b1f051a','b117d8dfb50946bfb8714fcb235845d2','0');
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('4d2f1971a6d947898a848aa775a45b0c','c92a08d19d514b6c8c462de3174b16a9','0');


-- 5.3.3.RELEASE 会话管理
INSERT INTO taresource (resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image) VALUES('8b00ee078064440cbd1ee34118ed2c52', '1d4e283ad5584e02811f6b188d3592bc', '会话管理', '', 'sysmg', 'sysmg.html#/sessionManagement', 130.000000000000000000000000000000, '40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/1d4e283ad5584e02811f6b188d3592bc/8b00ee078064440cbd1ee34118ed2c52', '银海软件/管理系统/系统管理/会话管理', '3', 'qq', NULL, '2', 0.000000000000000000000000000000, '1', '1', '1', NULL, '2023-01-12 00:00:00', '1', '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL);
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('60f05ed2d53c4676978971a62ab5b885', '会话管理', 'session/**', NULL, '0', '1', '2022-12-13 11:33:38', '1');
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('65062ea24d9f40e884314ad23429f717', '在线人员信息', 'session/onlineUser', '60f05ed2d53c4676978971a62ab5b885', '1', '1', '2022-12-13 11:35:16', '1');
INSERT INTO taurl (ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER) VALUES('9b061fa118ec4954b6db899e1332d3ac', '在线人员下线', 'session/offline', '60f05ed2d53c4676978971a62ab5b885', '1', '1', '2023-01-12 14:08:17', '1');
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('8b00ee078064440cbd1ee34118ed2c52', '60f05ed2d53c4676978971a62ab5b885', '0');
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('8b00ee078064440cbd1ee34118ed2c52', '65062ea24d9f40e884314ad23429f717', '0');
INSERT INTO taresourceurl (RESOURCEID, URLID, AUTHORITYPOLICY) VALUES('8b00ee078064440cbd1ee34118ed2c52', '9b061fa118ec4954b6db899e1332d3ac', '0');


-- 5.3.3.RELEASE 超级管理员账号默认密码修改为Aa111111，禁止使用弱口令
UPDATE tauser SET PASSWORD = '$2a$10$8aJYN7ebbqgMYR.rromR8uWsDUfOkPxcn.bJrsPXxrIQ/WxHycgPe' WHERE userid IN ('1', '2', '3');

-- 5.3.3.RELEASE 新增行为日志表
DROP TABLE IF EXISTS TASYSBEHAVIORLOG;
CREATE TABLE TASYSBEHAVIORLOG (
                                  ID VARCHAR2(36) NOT NULL  ,
                                  LOGINWAY VARCHAR2(36)    ,
                                  CLIENTID VARCHAR2(36)    ,
                                  USERID VARCHAR2(36)   ,
                                  OPERATCONTET VARCHAR2(256)    ,
                                  BUSINESSID VARCHAR2(36)    ,
                                  CURRENTOPERATEID NUMBER(20)  ,
                                  MENUID VARCHAR2(36)    ,
                                  SIAPPID VARCHAR2(36)   ,
                                  BEGINTIME VARCHAR2(36)    ,
                                  ENDTIME VARCHAR2(36)  ,
                                  TIMEUSED VARCHAR2(36)    ,
                                  OPERATETYPE VARCHAR2(36)    ,
                                  OPERATEOBJECTID VARCHAR2(36)    ,
                                  BUSINESSTYPE VARCHAR2(256)    ,
                                  SUCCESSFLAG VARCHAR2(36)    ,
                                  TRACEID VARCHAR2(36),
                                  CONTENT   VARCHAR2(1500),
);
COMMENT ON TABLE TASYSBEHAVIORLOG IS '行为日志表';
COMMENT ON COLUMN TASYSBEHAVIORLOG.ID IS '主键';
COMMENT ON COLUMN TASYSBEHAVIORLOG.LOGINWAY IS '登录方式';
COMMENT ON COLUMN TASYSBEHAVIORLOG.CLIENTID IS '客户端id';
COMMENT ON COLUMN TASYSBEHAVIORLOG.USERID IS '经办人id';
COMMENT ON COLUMN TASYSBEHAVIORLOG.BUSINESSID IS '业务编号';
COMMENT ON COLUMN TASYSBEHAVIORLOG.CURRENTOPERATEID IS '当前交易序号';
COMMENT ON COLUMN TASYSBEHAVIORLOG.MENUID IS '菜单编号';
COMMENT ON COLUMN TASYSBEHAVIORLOG.SIAPPID IS '应用编号';
COMMENT ON COLUMN TASYSBEHAVIORLOG.BEGINTIME IS '开始时间';
COMMENT ON COLUMN TASYSBEHAVIORLOG.ENDTIME IS '结束时间';
COMMENT ON COLUMN TASYSBEHAVIORLOG.TIMEUSED IS '执行时间';
COMMENT ON COLUMN TASYSBEHAVIORLOG.OPERATETYPE IS '操作对象类型';
COMMENT ON COLUMN TASYSBEHAVIORLOG.OPERATEOBJECTID IS '操作对象ID';
COMMENT ON COLUMN TASYSBEHAVIORLOG.BUSINESSTYPE IS '业务类型';
COMMENT ON COLUMN TASYSBEHAVIORLOG.SUCCESSFLAG IS '成功标志';
COMMENT ON COLUMN TASYSBEHAVIORLOG.TRACEID IS '链路ID';
COMMENT ON COLUMN TASYSBEHAVIORLOG.OPERATCONTET IS '行为描述';
COMMENT ON COLUMN TASYSBEHAVIORLOG.CONTENT IS '请求参数内容';
-- 5.3.3.RELEASE 新增行为日志细节表
DROP TABLE IF EXISTS TASYSBEHAVIORLOGDETAIL;
CREATE TABLE TASYSBEHAVIORLOGDETAIL (
                                        ID VARCHAR2(36) NOT NULL,
                                        CURRENTOPERATEID NUMBER(20)  ,
                                        OPERATEOBJECTID VARCHAR2(36)   ,
                                        OPERATCONTET VARCHAR2(256)   ,
                                        OPERATETYPE VARCHAR2(36)   ,
                                        BUSINESSTYPE VARCHAR2(256)  ,
                                        BEGINTIME VARCHAR2(36)    ,
                                        ENDTIME VARCHAR2(36)    ,
                                        TIMEUSED VARCHAR2(36)   ,
                                        LOGINWAY VARCHAR2(36)   ,
                                        CLIENTID VARCHAR2(36)    ,
                                        USERID VARCHAR2(36)   ,
                                        BUSINESSID VARCHAR2(36)    ,
                                        SUCCESSFLAG VARCHAR2(36)
);
COMMENT ON TABLE TASYSBEHAVIORLOGDETAIL IS '行为日志细节表';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.ID IS '主键';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.CURRENTOPERATEID IS '当前交易序号';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.OPERATEOBJECTID IS '操作对象ID';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.OPERATCONTET IS '行为描述';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.OPERATETYPE IS '操作对象类型';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.BUSINESSTYPE IS '操作对象编码';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.BEGINTIME IS '开始时间';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.ENDTIME IS '结束时间';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.TIMEUSED IS '执行时间';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.LOGINWAY IS '登录方式';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.CLIENTID IS '客户端id';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.USERID IS '经办人id';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.BUSINESSID IS '业务编号';
COMMENT ON COLUMN TASYSBEHAVIORLOGDETAIL.SUCCESSFLAG IS '成功标志';
-- 5.3.3.RELEASE 行为日志resource
INSERT INTO taresource
(resourceid, presourceid, name, code, syscode, url, orderno, idpath, namepath, resourcelevel, icon, iconcolor, securitypolicy, securitylevel, resourcetype, effective, isdisplay, isfiledscontrol, createdate, createuser, uiauthoritypolicy, field01, field02, field03, field04, field05, field06, field07, field08, field09, field10, workbench, image)
VALUES('bdcd5003137943299f69b36e55f2dc66', '1d4e283ad5584e02811f6b188d3592bc', '行为日志', NULL, 'sysmg', 'logmg.html#/opLog', 140.000000000000000000000000000000, '40337bdecb19484ebeb39d6c21aaca26/0415d44401b24605a21b589b6aaa349e/1d4e283ad5584e02811f6b188d3592bc/bdcd5003137943299f69b36e55f2dc66', '银海软件/管理系统/系统管理/行为日志', '3', NULL, NULL, '2', 0.000000000000000000000000000000, '1', '1', '1', NULL, '2023-03-20 00:00:00', '1', '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL);
INSERT INTO taurl
(ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER)
VALUES('0d8cabb5ed624eed9686a87e3dbb5620', '行为日志分页查询', 'oplogRestService/page', '0170b4dc35c34023a838c5f09aa68bd0', '1', '1', '2023-05-08 11:01:02', '1');
INSERT INTO taurl
(ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER)
VALUES('0170b4dc35c34023a838c5f09aa68bd0', '行为日志', 'oplogRestService/**', NULL, '0', '1', '2023-05-08 11:00:42', '1');
INSERT INTO taresourceurl
(RESOURCEID, URLID, AUTHORITYPOLICY)
VALUES('bdcd5003137943299f69b36e55f2dc66', '0170b4dc35c34023a838c5f09aa68bd0', '0');
INSERT INTO taresourceurl
(RESOURCEID, URLID, AUTHORITYPOLICY)
VALUES('bdcd5003137943299f69b36e55f2dc66', '0d8cabb5ed624eed9686a87e3dbb5620', '0');

-- 5.3.3.RELEASE 组织，人员缓存清除
INSERT INTO taurl
(ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER)
VALUES('6ae920d5f8ac419f96006055b6aab350', '获取是否开启组织机构缓存', 'org/orguser/orgManagementRestService/getOpenOrgCache', 'aa60a746c0a24640a06dea49a4d42572', '1', '1', '2023-04-23 15:42:50', '1');
INSERT INTO taurl
(ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER)
VALUES('48503f8ac8ec49a8ab43daf32f7257c9', '清除组织缓存', 'org/orguser/orgManagementRestService/clearOrgCache', 'aa60a746c0a24640a06dea49a4d42572', '1', '1', '2023-04-23 15:42:05', '1');
INSERT INTO taurl
(ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER)
VALUES('25bb2528eb9f44bf95bd740f7fd26d3b', '获取是否开启缓存', 'org/orguser/userManagementRestService/getOpenUserCache', 'b32b96d0142d4fea8d5fd8a58011c0c2', '1', '1', '2023-04-23 15:41:04', '1');
INSERT INTO taurl
(ID, NAME, URL, NAMESPACE, TYPE, EFFECTIVE, CREATETIME, CREATEUSER)
VALUES('be02802280924df0a7549418caf28a00', '清除人员缓存', 'org/orguser/userManagementRestService/clearUserCache', 'b32b96d0142d4fea8d5fd8a58011c0c2', '1', '1', '2023-04-23 15:40:41', '1');

INSERT INTO taresourceurl
(RESOURCEID, URLID, AUTHORITYPOLICY)
VALUES('daceeff8a97b46cb9573b93ba3a5a792', '25bb2528eb9f44bf95bd740f7fd26d3b', '0');
INSERT INTO taresourceurl
(RESOURCEID, URLID, AUTHORITYPOLICY)
VALUES('1e706f26bc144c1da12022359c238053', '48503f8ac8ec49a8ab43daf32f7257c9', '0');
INSERT INTO taresourceurl
(RESOURCEID, URLID, AUTHORITYPOLICY)
VALUES('1e706f26bc144c1da12022359c238053', '6ae920d5f8ac419f96006055b6aab350', '0');
INSERT INTO taresourceurl
(RESOURCEID, URLID, AUTHORITYPOLICY)
VALUES('daceeff8a97b46cb9573b93ba3a5a792', 'be02802280924df0a7549418caf28a00', '0');

-- 5.3.3.RELEASE 人员新增拼音查询
ALTER TABLE tauser ADD spell VARCHAR2(1000);
