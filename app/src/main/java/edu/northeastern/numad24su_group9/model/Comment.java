package edu.northeastern.numad24su_group9.model;

public class Comment {
    private String commentId;
    private String commentText;
    private long timestamp;
    private String commenterName;

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String commentId, String commentText, long timestamp, String commenterName) {
        this.commentId = commentId;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.commenterName = commenterName;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}