type FormatMode = "full" | "hour" | "phone" | "date" | "stringed";

const formatDateTime = (date: Date, mode: FormatMode = "full") => {
  if (mode === "hour") {
    return date.toLocaleString("en-US", {
      hour: "numeric",
      hour12: true,
    });
  } else if (mode === "date") {
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }
  return date.toLocaleString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });
};

const formatPhone = (phone: string | undefined | null): string => {
  if (!phone) return "N/A";

  const cleaned = phone.replace(/\D/g, "");

  if (cleaned.length === 13 && cleaned.startsWith("20")) {
    return `+20 ${cleaned.slice(3, 5)} ${cleaned.slice(5, 9)} ${cleaned.slice(
      9
    )}`;
  } else if (cleaned.length === 12 && cleaned.startsWith("20")) {
    return `+20 ${cleaned.slice(2, 4)} ${cleaned.slice(4, 8)} ${cleaned.slice(
      8
    )}`;
  } else if (
    cleaned.length === 11 &&
    (cleaned.startsWith("01") ||
      cleaned.startsWith("10") ||
      cleaned.startsWith("11") ||
      cleaned.startsWith("12") ||
      cleaned.startsWith("15"))
  ) {
    return `+20 ${cleaned.slice(1, 3)} ${cleaned.slice(3, 7)} ${cleaned.slice(
      7
    )}`;
  }

  // US phone numbers (original logic)
  else if (cleaned.length === 10) {
    return `+1 (${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(
      6
    )}`;
  } else if (cleaned.length === 11 && cleaned.startsWith("1")) {
    return `+1 (${cleaned.slice(1, 4)}) ${cleaned.slice(4, 7)}-${cleaned.slice(
      7
    )}`;
  }

  return phone;
};

const format = (data: unknown, mode: FormatMode): string => {
  try {
    switch (mode) {
      case "phone":
        return formatPhone(data as string);
      case "hour":
      case "date":
      case "full": {
        if (!data) return "N/A";
        const dateObj =
          typeof data === "string" ? new Date(data) : (data as Date);
        if (isNaN(dateObj.getTime())) return "Invalid Date";
        return formatDateTime(dateObj, mode);
      }
      case "stringed": {
        const intoDate = new Date(data as string);
        if (isNaN(intoDate.getTime())) return "Invalid Date";
        return formatDateTime(intoDate, "full");
      }
      default:
        return String(data || "N/A");
    }
  } catch {
    return "N/A";
  }
};

function toLocalISOString(date: Date): string {
  const pad = (n: number) => n.toString().padStart(2, "0");

  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  const seconds = pad(date.getSeconds());
  const ms = date.getMilliseconds().toString().padStart(3, "0");

  // Same shape as ISO string, but using local parts
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}.${ms}Z`;
}

export { formatDateTime, toLocalISOString };
export default format;
