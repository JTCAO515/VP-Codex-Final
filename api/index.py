import traceback

try:
    from main import app
except Exception as e:
    # In Vercel serverless, print goes to logs
    print(f"IMPORT ERROR: {e}", flush=True)
    traceback.print_exc()
    raise
