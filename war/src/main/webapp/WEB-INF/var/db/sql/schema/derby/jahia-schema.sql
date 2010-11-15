
    alter table jahia_pwd_policy_rule_params 
        drop constraint FKBE451EF45A0DB19B;

    alter table jahia_pwd_policy_rules 
        drop constraint FK2BC650026DA1D1E6;

    drop table jahia_contenthistory;

    drop table jahia_db_test;

    drop table jahia_installedpatch;

    drop table jahia_pwd_policies;

    drop table jahia_pwd_policy_rule_params;

    drop table jahia_pwd_policy_rules;

    drop table jahia_version;

    create table jahia_contenthistory (
        id varchar(32) not null,
        entry_action varchar(255),
        entry_date timestamp,
        message varchar(255),
        entry_path clob,
        property_name varchar(50),
        user_key varchar(255),
        uuid varchar(36),
        primary key (id)
    );

    create table jahia_db_test (
        testfield varchar(255) not null,
        primary key (testfield)
    );

    create table jahia_installedpatch (
        install_number integer not null,
        build integer,
        install_date timestamp,
        name varchar(100),
        result_code integer,
        primary key (install_number)
    );

    create table jahia_pwd_policies (
        jahia_pwd_policy_id integer not null,
        name varchar(255) not null,
        primary key (jahia_pwd_policy_id)
    );

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id integer not null,
        name varchar(50) not null,
        position_index integer not null,
        param_type char(1) not null,
        param_value varchar(255),
        jahia_pwd_policy_rule_id integer not null,
        primary key (jahia_pwd_policy_rule_param_id)
    );

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id integer not null,
        rule_action char(1) not null,
        active smallint not null,
        rule_condition clob(1048576) not null,
        evaluator char(1) not null,
        last_rule smallint not null,
        name varchar(255) not null,
        periodical smallint not null,
        position_index integer not null,
        jahia_pwd_policy_id integer not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_version (
        install_number integer not null,
        build integer,
        install_date timestamp,
        release_number varchar(20),
        primary key (install_number)
    );

    alter table jahia_pwd_policy_rule_params 
        add constraint FKBE451EF45A0DB19B 
        foreign key (jahia_pwd_policy_rule_id) 
        references jahia_pwd_policy_rules;

    alter table jahia_pwd_policy_rules 
        add constraint FK2BC650026DA1D1E6 
        foreign key (jahia_pwd_policy_id) 
        references jahia_pwd_policies;
