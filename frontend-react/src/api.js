let session = { accessToken: "", user: null };
export const getSession = () => session;
export function clearSession() {
  session = { accessToken: "", user: null };
}
function authorizationHeaders() {
  if (!session.accessToken) return {};
  return { Authorization: `Bearer ${session.accessToken}` };
}
export async function request(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...authorizationHeaders(),
      ...options.headers,
    },
  });
  const data = response.status === 204 ? null : await response.json();
  if (!response.ok) {
    if (response.status === 401) clearSession();
    const message = errorMessage(response.status, data, path);
    const error = new Error(message);
    error.status = response.status;
    error.code = data.code;
    throw error;
  }
  return data;
}

function errorMessage(status, data, path) {
  if (status === 401) {
    if (data?.code === "INVALID_CREDENTIALS" || path === "/auth/login") {
      return "이메일 또는 비밀번호를 확인하세요.";
    }
    if (data?.code === "TOKEN_REQUIRED") return "로그인이 필요합니다.";
    if (data?.code === "TOKEN_EXPIRED") return "로그인 시간이 만료되었습니다. 다시 로그인하세요.";
    if (data?.code === "INVALID_TOKEN") return "인증 정보가 유효하지 않습니다. 다시 로그인하세요.";
    return "인증에 실패했습니다. 다시 로그인하세요.";
  }
  if (status === 400) return data?.message || "입력 내용을 확인하고 다시 시도하세요.";
  if (status === 403) return "본인이 작성한 게시글만 수정하거나 삭제할 수 있습니다.";
  if (status === 404) return "게시글을 찾을 수 없습니다. 목록을 새로 고칩니다.";
  return data?.message || data?.code || `HTTP ${status}`;
}
export async function login(email, password) {
  const result = await request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
  session = { accessToken: result.accessToken, user: result.user };
  return result.user;
}
export function canEdit(post) {
  return session.user?.id === post?.authorId;
}
export const api = {
  list: () => request("/posts"),
  detail: (id) => request(`/posts/${id}`),
  create: (input) =>
    request("/posts", { method: "POST", body: JSON.stringify(input) }),
  update: (id, input) =>
    request(`/posts/${id}`, { method: "PUT", body: JSON.stringify(input) }),
  remove: (id) => request(`/posts/${id}`, { method: "DELETE" }),
  me: () => request("/me"),
};
