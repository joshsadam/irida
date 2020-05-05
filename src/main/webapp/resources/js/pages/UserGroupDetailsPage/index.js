import React, { useContext, useEffect, useReducer } from "react";
import { PageWrapper } from "../../components/page/PageWrapper";
import { useNavigate } from "@reach/router";
import {
  getUserGroupDetails,
  updateUserGroupDetails,
} from "../../apis/users/groups";
import { Select, Table, Typography } from "antd";
import { BasicList } from "../../components/lists";
import { formatInternationalizedDateTime } from "../../utilities/date-utilities";
import {
  UserGroupRolesProvider,
  UserGroupRolesContext,
} from "../../contexts/UserGroupRolesContext";
import { UserGroupRole } from "../../components/roles/UserGroupRole";

const { Paragraph } = Typography;

function UserGroupMembersTable({ members, canManage, groupId }) {
  const { roles, getRoleFromKey } = useContext(UserGroupRolesContext);

  const columns = [
    {
      dataIndex: "name",
      title: "Member Name",
    },
    {
      title: "role",
      dataIndex: "role",
      width: 200,
      render(text, user) {
        return (
          <UserGroupRole user={user} canManage={canManage} groupId={groupId} />
        );
      },
    },
    {
      title: "Joined",
      dataIndex: "createdDate",
      width: 200,
      render(text) {
        return formatInternationalizedDateTime(text);
      },
    },
  ];

  return (
    <Table
      pagination={{ hideOnSinglePage: true }}
      columns={columns}
      dataSource={members}
    />
  );
}

const reducer = (state, action) => {
  switch (action.type) {
    case "load":
      return { ...state, loading: false, ...action.payload };
    case "update":
      return { ...state, ...action.payload };
    default:
      return { ...state };
  }
};

export function UserGroupDetails({ id }) {
  const [state, dispatch] = useReducer(reducer, { loading: true });

  const navigate = useNavigate();

  useEffect(() => {
    getUserGroupDetails(id).then((response) =>
      dispatch({ type: "load", payload: response })
    );
  }, [id]);

  const updateField = (field, value) => {
    updateUserGroupDetails({ id, field, value }).then(() =>
      dispatch({ type: "update", payload: { [field]: value } })
    );
  };

  const fields = state.loading
    ? []
    : [
        {
          title: "Name",
          desc: state.canManage ? (
            <Paragraph
              editable={{ onChange: (value) => updateField("name", value) }}
            >
              {state.name}
            </Paragraph>
          ) : (
            state.name
          ),
        },
        {
          title: "Description",
          desc: state.canManage ? (
            <Paragraph
              editable={{
                onChange: (value) => updateField("description", value),
              }}
            >
              {state.description}
            </Paragraph>
          ) : (
            state.description
          ),
        },
        {
          title: "Members",
          desc: (
            <UserGroupRolesProvider>
              <UserGroupMembersTable
                members={state.members}
                canManage={state.canManage}
                groupId={id}
              />
            </UserGroupRolesProvider>
          ),
        },
      ];

  return (
    <PageWrapper
      title={"User Groups"}
      onBack={() => navigate("/groups", { replace: true })}
    >
      <BasicList dataSource={fields} />
    </PageWrapper>
  );
}
