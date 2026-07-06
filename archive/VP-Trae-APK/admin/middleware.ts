import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";
import { ADMIN_AUTH_COOKIE } from "./lib/auth";

function hasAdminSession(request: NextRequest) {
  return Boolean(request.cookies.get(ADMIN_AUTH_COOKIE)?.value);
}

export function middleware(request: NextRequest) {
  const { pathname, search } = request.nextUrl;
  const isLoginPage = pathname === "/login";
  const isProtectedUsersRoute = pathname === "/users" || pathname.startsWith("/users/");

  if (isProtectedUsersRoute && !hasAdminSession(request)) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("from", `${pathname}${search}`);
    return NextResponse.redirect(loginUrl);
  }

  if (isLoginPage && hasAdminSession(request)) {
    return NextResponse.redirect(new URL("/users", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/login", "/users/:path*"]
};
