export default function StarRating({ rating = 0, count = 0, size = "text-sm" }) {
  if (!rating || rating <= 0) {
    return <span className={`text-slate-400 ${size}`}>No reviews yet</span>;
  }

  const rounded = Math.round(rating);
  const stars = Array.from({ length: 5 }, (_, i) => (i < rounded ? "★" : "☆"));

  return (
    <span className={`inline-flex items-center gap-1 ${size}`}>
      <span className="text-amber-500 tracking-tight">{stars.join("")}</span>
      <span className="text-slate-500">
        {rating.toFixed ? rating.toFixed(1) : rating} {count > 0 && `(${count})`}
      </span>
    </span>
  );
}
