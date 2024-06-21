
-- 删除了tataggroup表, 内容添加为字典码值, 新增角色类型标签
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('标签类型', 'TAGTYPE', '用户', '001', NULL, 1, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('标签类型', 'TAGTYPE', '组织', '002', NULL, 2, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('标签类型', 'TAGTYPE', '角色', '003', NULL, 3, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

-- 新增账号来源字典
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('账户来源', 'ACCOUNTSOURCE', '系统管理录入', '1', NULL, 1, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('账户来源', 'ACCOUNTSOURCE', '账号申请审核', '2', NULL, 2, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('账户来源', 'ACCOUNTSOURCE', '注册通道注册', '3', NULL, 3, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

-- 修改账号类型字典
DELETE FROM tadict WHERE type = 'ACCOUNTTYPE';
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('账户类型', 'ACCOUNTTYPE', '经办管理账号', '1', NULL, 1, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('账户类型', 'ACCOUNTTYPE', '自然人账号', '2', NULL, 2, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

-- 新增账号申请类型字典
INSERT INTO tadict (NAME,`TYPE`,LABEL,VALUE,PARENTVALUE,sort,AUTHORITY,cssclass,CSSSTYLE,REMARKS,CREATETIME,CREATEUSER,VERSION,STATUS,FIELD01,FIELD02,FIELD03,FIELD04,FIELD05,`SYSTEM`,NEWTYPE) VALUES ('申请类型','APPLY_TYPE','账号申请','1',NULL,10,'0',NULL,NULL,NULL,'2023-07-31 00:00:00','1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');

-- 新增账号申请状态字典
INSERT INTO tadict (NAME,`TYPE`,LABEL,VALUE,PARENTVALUE,sort,AUTHORITY,cssclass,CSSSTYLE,REMARKS,CREATETIME,CREATEUSER,VERSION,STATUS,FIELD01,FIELD02,FIELD03,FIELD04,FIELD05,`SYSTEM`,NEWTYPE) VALUES ('申请状态','APPLY_STATUS','待提交','1',NULL,10,'0',NULL,NULL,NULL,'2023-07-31 00:00:00','1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');
INSERT INTO tadict (NAME,`TYPE`,LABEL,VALUE,PARENTVALUE,sort,AUTHORITY,cssclass,CSSSTYLE,REMARKS,CREATETIME,CREATEUSER,VERSION,STATUS,FIELD01,FIELD02,FIELD03,FIELD04,FIELD05,`SYSTEM`,NEWTYPE) VALUES ('申请状态','APPLY_STATUS','待审核','2',NULL,20,'0',NULL,NULL,NULL,'2023-07-31 00:00:00','1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');
INSERT INTO tadict (NAME,`TYPE`,LABEL,VALUE,PARENTVALUE,sort,AUTHORITY,cssclass,CSSSTYLE,REMARKS,CREATETIME,CREATEUSER,VERSION,STATUS,FIELD01,FIELD02,FIELD03,FIELD04,FIELD05,`SYSTEM`,NEWTYPE) VALUES ('申请状态','APPLY_STATUS','审核通过','3',NULL,30,'0',NULL,NULL,NULL,'2023-07-31 00:00:00','1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');
INSERT INTO tadict (NAME,`TYPE`,LABEL,VALUE,PARENTVALUE,sort,AUTHORITY,cssclass,CSSSTYLE,REMARKS,CREATETIME,CREATEUSER,VERSION,STATUS,FIELD01,FIELD02,FIELD03,FIELD04,FIELD05,`SYSTEM`,NEWTYPE) VALUES ('申请状态','APPLY_STATUS','审核不通过','4',NULL,40,'0',NULL,NULL,NULL,'2023-07-31 00:00:00','1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');
INSERT INTO tadict (NAME,`TYPE`,LABEL,VALUE,PARENTVALUE,sort,AUTHORITY,cssclass,CSSSTYLE,REMARKS,CREATETIME,CREATEUSER,VERSION,STATUS,FIELD01,FIELD02,FIELD03,FIELD04,FIELD05,`SYSTEM`,NEWTYPE) VALUES ('申请状态','APPLY_STATUS','作废','5',NULL,50,'0',NULL,NULL,NULL,'2023-07-31 00:00:00','1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');


-- 新增权限标识字典类型
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, CREATETIME, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('权限标识类型', 'AUTHORITY', '默认', '0', NULL, 1, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

-

-- 更新dict视图
create OR REPLACE view v_dict (name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks,
                               createtime, createuser, version, status, field01, field02, field03, field04, field05,
                               `SYSTEM`, newtype) as
select x0.name,
       x0.type,
       x0.label,
       x0.value,
       x0.parentvalue,
       x0.sort,
       x0.authority,
       x0.cssclass,
       x0.cssstyle,
       x0.remarks,
       x0.createtime,
       x0.createuser,
       x0.version,
       x0.status,
       x0.field01,
       x0.field02,
       x0.field03,
       x0.field04,
       x0.field05,
       x0.`SYSTEM`,
       x0.newtype
from tadict x0;

-- 新增行政区划、自定义组织操作日志字典

INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作对象类型', 'OPOBJTYPE', '行政区划', '09', NULL, 90, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作对象类型', 'OPOBJTYPE', '自定义组织', '10', NULL, 100, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '新增行政区划', '51', NULL, 510, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '编辑行政区划', '52', NULL, 520, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '禁用行政区划', '53', NULL, 530, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '启用行政区划', '54', NULL, 540, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '删除行政区划', '55', NULL, 550, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '新增自定义组织', '56', NULL, 560, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '编辑自定义组织', '57', NULL, 570, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '禁用自定义组织', '58', NULL, 580, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '启用自定义组织', '59', NULL, 590, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '删除自定义组织', '60', NULL, 600, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '相似权限授权', '67', NULL, 670, '0', NULL, NULL, NULL, NOW(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('菜单打开方式','RESOURCEOPENMODE','工作页','1',NULL,10,'0',NULL,NULL,NULL,NOW(),'1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('菜单打开方式','RESOURCEOPENMODE','浏览器页','2',NULL,20,'0',NULL,NULL,NULL,NOW(),'1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');
INSERT INTO tadict(NAME, TYPE, label, VALUE, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, VERSION, STATUS, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('菜单打开方式','RESOURCEOPENMODE','弹窗','3',NULL,30,'0',NULL,NULL,NULL,NOW(),'1','0','1',NULL,NULL,NULL,NULL,NULL,'1','0');

-- 添加审核管理员权限菜单参数
INSERT INTO taparam(PARAMID, PARAMNAME, VALUESCOPE, CODE, PARAMDESC, VALUE, RESOURCEID) VALUES('1000000442', 'isExamine', '1,0', '', '是否', '1', 'e7542892ef424e809c3bb8cfa8c0051b');

-- 用户可管理字段标签类型字典
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('组织页面标签类型', 'EXTENDORGTAG', '组织信息', '1', NULL, 10, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('组织页面标签类型', 'EXTENDORGTAG', '其他信息', '2', NULL, 20, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('组织页面标签类型', 'EXTENDORGTAG', '扩展信息', '3', NULL, 30, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('功能资源页面标签类型', 'EXTENDRESOURCETAG', '基本信息', '1', NULL, 10, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('功能资源页面标签类型', 'EXTENDRESOURCETAG', '更多信息', '2', NULL, 20, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('用户页面标签类型', 'EXTENDUSERTAG', '账号信息', '1', NULL, 10, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('用户页面标签类型', 'EXTENDUSERTAG', '人员信息', '2', NULL, 20, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('用户页面标签类型', 'EXTENDUSERTAG', '其他信息', '3', NULL, 30, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');



INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '更改使用权限有效时间', '90', NULL, 870, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '账号删除和角色的关联关系', '89', NULL, 860, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '账号新增和角色的关联关系', '88', NULL, 850, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '组织删除和角色的关联关系', '87', NULL, 840, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '组织新增和角色的关联关系', '86', NULL, 830, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '角色删除和组织的关联关系', '85', NULL, 820, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '角色新增和组织的关联关系', '84', NULL, 810, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '修改角色子组织可见性', '83', NULL, 800, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '更改账号自定义组织权限（收回）', '82', NULL, 790, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '更改账号自定义组织权限（授予）', '81', NULL, 780, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict
(NAME, `TYPE`, LABEL, VALUE, PARENTVALUE, sort, AUTHORITY, cssclass, CSSSTYLE, REMARKS, CREATETIME, CREATEUSER, VERSION, STATUS, FIELD01, FIELD02, FIELD03, FIELD04, FIELD05, `SYSTEM`, NEWTYPE)
VALUES('操作类型', 'OPTYPE', '管理员角色组织管理范围类型修改', '80', NULL, 770, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');

insert into TAREDISSEQUENCE(BIZ_TAG, START_INDEX, MAX_ID, STEP) values ('HIBERNATE_SEQUENCE',0,0,100);

INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '彻底删除', '62', NULL, 750, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '回收站还原', '65', NULL, 760, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '加入回收站', '64', NULL, 680, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '人员导入', '68', NULL, 690, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '导入组织', '69', NULL, 700, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '资源菜单导入', '70', NULL, 710, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '导出人员', '71', NULL, 720, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '导出组织', '72', NULL, 730, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
INSERT INTO tadict(name, type, label, value, parentvalue, sort, authority, cssclass, cssstyle, remarks, createtime, createuser, version, status, field01, field02, field03, field04, field05, `SYSTEM`, newtype) VALUES ('操作类型', 'OPTYPE', '导出资源菜单', '73', NULL, 740, '0', NULL, NULL, NULL, now(), '1', '0', '1', NULL, NULL, NULL, NULL, NULL, '1', '0');
