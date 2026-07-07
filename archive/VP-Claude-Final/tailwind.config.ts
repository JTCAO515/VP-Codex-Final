import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: {
          cream: "#f5ecdd",
          paper: "#f7f0e2",
          ochre: "#9c7d54",
          umber: "#6e5634",
          cinnabar: "#a23728",
          gold: "#b8862c",
        },
      },
      fontFamily: {
        display: ["var(--font-display)"],
        body: ["var(--font-body)"],
      },
    },
  },
  plugins: [],
};
export default config;
