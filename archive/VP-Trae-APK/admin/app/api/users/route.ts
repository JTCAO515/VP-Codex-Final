import { NextResponse } from "next/server";
import { createServerSupabaseAdminClient } from "../../../lib/supabase/server";

export async function GET() {
  try {
    const supabase = createServerSupabaseAdminClient();
    const { data, error } = await supabase
      .from("user_profiles")
      .select("*")
      .order("created_at", { ascending: false });

    if (error) {
      return NextResponse.json({ error: error.message }, { status: 500 });
    }

    return NextResponse.json({ data: data ?? [] });
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : "加载用户列表时出现未知错误。" },
      { status: 500 }
    );
  }
}
