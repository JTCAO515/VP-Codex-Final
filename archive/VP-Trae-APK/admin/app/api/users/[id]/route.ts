import { NextRequest, NextResponse } from "next/server";
import { createServerSupabaseAdminClient } from "../../../../lib/supabase/server";
import { normalizeUpdatePayload } from "../../../../lib/user-api";

export async function GET(_request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  try {
    const supabase = createServerSupabaseAdminClient();
    const { data, error } = await supabase.from("user_profiles").select("*").eq("id", id).maybeSingle();

    if (error) {
      return NextResponse.json({ error: error.message }, { status: 500 });
    }

    if (!data) {
      return NextResponse.json({ error: "用户不存在。" }, { status: 404 });
    }

    return NextResponse.json({ data });
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : "加载用户详情时出现未知错误。" },
      { status: 500 }
    );
  }
}

export async function PATCH(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  try {
    const body = (await request.json()) as Record<string, unknown>;
    const updates = normalizeUpdatePayload(body);

    if (Object.keys(updates).length === 0) {
      return NextResponse.json({ error: "未提供可更新字段。" }, { status: 400 });
    }

    const supabase = createServerSupabaseAdminClient();
    const { data, error } = await supabase
      .from("user_profiles")
      .update(updates)
      .eq("id", id)
      .select("*")
      .maybeSingle();

    if (error) {
      return NextResponse.json({ error: error.message }, { status: 500 });
    }

    if (!data) {
      return NextResponse.json({ error: "用户不存在。" }, { status: 404 });
    }

    return NextResponse.json({ data });
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : "更新用户资料时出现未知错误。" },
      { status: 500 }
    );
  }
}
