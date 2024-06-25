

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

insert into TAREDISSEQUENCE(BIZ_TAG, START_INDEX, MAX_ID, STEP) values ('HIBERNATE_SEQUENCE',0,0,100);

