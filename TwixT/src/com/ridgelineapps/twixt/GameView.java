/*
 * Copyright (C) 2012 TwixT for Tablets (http://code.google.com/p/twixt-for-tablets)
 * 
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *   
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ridgelineapps.twixt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

public class GameView extends View {
    
//    public static boolean drawSides = false;
    MyBoard board;
    float scale;
    int border = 5;
    
    int xOffset = 0;
    int yOffset = 0;
    
    int width = -1;
    int height = -1;
    
    boolean showingWinner;
    
    boolean showAreaLines;
    
    Theme theme;
    
    PointF placingPegLoc;
    int[] lastPlacingBoardLoc;
    
    static boolean DRAW_SHADOW_PEG = false;
    PointF shadowPegLoc;
    
    PointF touch;
    
    boolean showLastPlacement = true;
    
	float upperLeftX;
	float upperLeftY;
    float lowerRightX;
    float lowerRightY;
    
    float board_x1;
    float board_y1;
    float board_x2;
    float board_y2;
    
    boolean confirmActive = false;

    public GameView(Context context, MyBoard board, Theme theme) {
        super(context);
        showingWinner = false;
        this.board = board;
        
        this.theme = theme;
    }
    
    protected void onDraw(Canvas canvas) {
//		Commenting out dynamic width and height adjusting since it seems to not work properly when coming back to the app from sleep. 
//    	Setting on first draw and keeping does look like it works.
//      if(width != canvas.getWidth() || height != canvas.getHeight()) { 
//    		resetValues(canvas);
//    	}
//    	
//    	Setting on first draw and keeping does look like it works. 
    	if(width == -1 || height == -1) {
    		resetValues(canvas);
    	}

    	theme.setPaints(board.winner);
    	theme.drawBoard(canvas, new RectF(board_x1, board_y1, board_x2, board_y2), scale);
        
        if(board.winner == 0) {
            if(confirmActive) {
                RectF bounds = getOKBounds(board.turn, 0);
                theme.drawOKPrompt(canvas, bounds, board.turn);                
                bounds = getCancelBounds(board.turn, 0);
                theme.drawCancelPrompt(canvas, bounds, board.turn);                
            }
            
            RectF bounds = getPromptBounds(board.turn, 0);
            theme.drawPrompt(canvas, bounds, board.turn);
        }

        theme.drawBorderLine(canvas, new float[]{ upperLeftX, upperLeftY }, new float[]{ lowerRightX, upperLeftY }, 2);
        theme.drawBorderLine(canvas, new float[]{ upperLeftX, lowerRightY }, new float[]{ lowerRightX, lowerRightY }, 2);
        
        theme.drawBorderLine(canvas, new float[]{ upperLeftX, upperLeftY }, new float[]{ upperLeftX, lowerRightY }, 1);
        theme.drawBorderLine(canvas, new float[]{ lowerRightX, upperLeftY }, new float[]{ lowerRightX, lowerRightY }, 1);
        
        // draw lines
        for(int x=0; x < board.size; x++) {
            for(int y=0; y < board.size; y++) {
            	for(int i = 1; i < 5; i++) {
            		int p = 0;
            		if(board.lines[x][y][i][1]) {
            			p = 1;
            		} 
            		else if(board.lines[x][y][i][2]) {
            			p = 2;
            		} 
            		
            		if(p > 0) {
            			int x2 = x;
            			int y2 = y;
            			
            			x2 += MyBoard.getDifference(i)[0];
                        y2 += MyBoard.getDifference(i)[1];
            			
		                float[] point = translateToScreen(x, y);
		                float[] point2 = translateToScreen(x2, y2);

		                theme.drawLine(canvas, point, point2, p, false);
		                
//	                    if(board.winner != 0 && (!board.winningPegs[x][y] || !board.winningPegs[x2][y2])) {
//	                        theme.darkenLine(canvas, point, point2, p);
//	                    }
            		}
            	}
            }
        }
        
        for(int x=0; x < board.size; x++) {
            for(int y=0; y < board.size; y++) {
                for(int i = 1; i < 5; i++) {
                    int p = 0;
                    if(board.futureLines[x][y][i][1]) {
                        p = 1;
                    } 
                    else if(board.futureLines[x][y][i][2]) {
                        p = 2;
                    } 
                    
                    if(p > 0) {
                        int x2 = x;
                        int y2 = y;
                        
                        x2 += MyBoard.getDifference(i)[0];
                        y2 += MyBoard.getDifference(i)[1];
                        
                        float[] point = translateToScreen(x, y);
                        float[] point2 = translateToScreen(x2, y2);

                        theme.drawLine(canvas, point, point2, p, true);
                        
//                        if(board.winner != 0 && (!board.winningPegs[x][y] || !board.winningPegs[x2][y2])) {
//                            theme.darkenLine(canvas, point, point2, p);
//                        }
                    }
                }
            }
        }
        
        // draw area lines
        if(showAreaLines) {
            int x1, y1, x2, y2, offsetX, offsetY;
            x1 = 1; y1 = 1;
            x2 = x1; y2 = y1;
            offsetX = 2;
            offsetY = 1;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
            x1 = 1; y1 = 1;
            x2 = x1; y2 = y1;
            offsetX = 1;
            offsetY = 2;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
            x1 = board.size - 2; y1 = 1;
            x2 = x1; y2 = y1;
            offsetX = -2;
            offsetY = 1;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
            x1 = board.size - 2; y1 = 1;
            x2 = x1; y2 = y1;
            offsetX = -1;
            offsetY = 2;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
            x1 = 1; y1 = board.size - 2;
            x2 = x1; y2 = y1;
            offsetX = 1;
            offsetY = -2;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
            x1 = 1; y1 = board.size - 2;
            x2 = x1; y2 = y1;
            offsetX = 2;
            offsetY = -1;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
            x1 = board.size - 2; y1 = board.size - 2;
            x2 = x1; y2 = y1;
            offsetX = -1;
            offsetY = -2;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
            x1 = board.size - 2; y1 = board.size - 2;
            x2 = x1; y2 = y1;
            offsetX = -2;
            offsetY = -1;
            while(x2 + offsetX < board.size - 1 && y2 + offsetY < board.size - 1 && x2 + offsetX > 1 && y2 + offsetY > 1) {
                x2 += offsetX;
                y2 += offsetY;
            } 
            theme.drawAreaLine(canvas,  translateToScreen(x1, y1), translateToScreen(x2, y2));
        }
        
        // draw pegs and holes
        for(int x=0; x < board.size; x++) {
            for(int y=0; y < board.size; y++) {
            	if((x == 0 && y == 0) || (x == 0 && y == board.size - 1) || (x == board.size - 1 && y == 0) || (x == board.size - 1 && y == board.size - 1)) {
            		continue;
            	}
            	
                float[] point = translateToScreen(x, y);
                // draw board
                theme.drawHole(canvas, point);
                // draw peg if needed
                int p = board.pegs[x][y];
            	if(p != 0) {
            		boolean darken = false; //(board.winner != 0 && !board.winningPegs[x][y]);
            		theme.drawPeg(canvas, point, p, darken);
            	}
            }
        }
        
        PointF peg = placingPegLoc;
        if(peg == null) {
//            peg = getStartingPosForOffboardPeg();
        }
        if(peg != null) {
            int p = board.turn;
            
            int[] boardLoc = translateToBoard(peg.x, peg.y);
            
            boolean posReal = board.isPositionReal(boardLoc[0], boardLoc[1]);
            if(posReal || getPromptBounds(p, 2).contains(peg.x, peg.y)) {
                if(!TwixtUtils.isCorner(board, boardLoc[0], boardLoc[1]) && (!posReal || board.isValid(boardLoc[0], boardLoc[1], p))) {
                    
                    if(touch == null || isYWithinBounds(touch.y)) {
                    	theme.drawFuturePeg(canvas, new float[]{ peg.x, peg.y }, p);
                    }
                }
            }
            
//            int buffer = (int) (scale * 3);
//            Rect clip = new Rect((int) (peg.x - buffer), (int) (peg.y - buffer), (int) (peg.x + buffer), (int) (peg.y + buffer));
//            canvas.clipRect(clip);
        }
        
        if(DRAW_SHADOW_PEG && shadowPegLoc != null) {
            if(touch == null || isYWithinBounds(touch.y)) {
            	theme.drawPegBeingPlaced(canvas, new float[]{ shadowPegLoc.x, shadowPegLoc.y });
            }
        }
        
        // draw cursor
        if(board.cursor[0] >= 0) {
            float[] point = translateToScreen(board.cursor[0], board.cursor[1]);
            theme.drawCrosshairs(canvas, upperLeftX, upperLeftY, lowerRightX, lowerRightY, point[0], point[1]);
            
//            int buffer = (int) (scale / 2);
//            Rect clip = new Rect((int) upperLeftX, (int) (point[1] - buffer), (int) lowerRightX, (int) (point[1] + buffer));
//            canvas.clipRect(clip);
//
//            clip = new Rect((int) (point[0] - buffer), (int) upperLeftY, (int) (point[0] + buffer), (int) lowerRightY);
//            canvas.clipRect(clip);
        }
        
        if(board.winner == 0 && showLastPlacement && board.lastTurnX != -1 && board.lastTurnY != -1) {
        	int p = board.pegs[board.lastTurnX][board.lastTurnY];
        	if(p > 0) {
	            float[] point = translateToScreen(board.lastTurnX, board.lastTurnY);
	            theme.drawLastPlacement(canvas, point[0], point[1], p);
        	}
        }
    }
    
    public void resetValues(Canvas canvas) {
    	width = canvas.getWidth();
    	height = canvas.getHeight();
    	
        int smallSide = Math.min(canvas.getWidth(), canvas.getHeight());
        int longSide = Math.max(canvas.getWidth(), canvas.getHeight());
        scale = (float) (smallSide - border * 2) / board.size;
        
        boolean bumpDotRadius = (board.size == 24);
        theme.resetValues(scale, bumpDotRadius);
        
        if(longSide == canvas.getWidth()) {
            xOffset = (longSide - smallSide) / 2; 
        } 
        else {
            yOffset = (longSide - smallSide) / 2; 
        }
     
        float[] zero_zero = translateToScreen(0, 0);
        float[] one_one = translateToScreen(1, 1); 
        float[] last_last = translateToScreen(board.size - 1, board.size - 1);
        float[] lastminusone_lastminusone = translateToScreen(board.size - 2, board.size - 2);
        
        upperLeftX = zero_zero[0] + (one_one[0] - zero_zero[0]) / 2;
        upperLeftY = zero_zero[1] + (one_one[1] - zero_zero[1]) / 2;
        lowerRightX = lastminusone_lastminusone[0] + (last_last[0] - lastminusone_lastminusone[0]) / 2;
        lowerRightY = lastminusone_lastminusone[1] + (last_last[1] - lastminusone_lastminusone[1]) / 2;
        
        board_x1 = zero_zero[0] - (one_one[0] - zero_zero[0]) / 2;
        board_y1 = zero_zero[1] - (one_one[1] - zero_zero[1]) / 2;
        board_x2 = last_last[0] + (last_last[0] - lastminusone_lastminusone[0]) / 2;
        board_y2 = last_last[1] + (last_last[1] - lastminusone_lastminusone[1]) / 2;
    }
    
    PointF getStartingPosForOffboardPeg() {
        if(board.winner != 0) {
            return null;
        }
        
        float f = 0.8f;
        if(board.turn == 1) {
            float[] p = translateToScreen(0, board.size - 1);
            return new PointF(p[0] * f, height / 2);
        } else if(board.turn == 2) {
            float[] p = translateToScreen(board.size - 1, board.size - 1);
            return new PointF(width - (width - p[0]) * f, height / 2);
        }
        return null;
    }
    
    public boolean isYWithinBounds(float y) {
        return y > 4 && y < height - 4;
    }
    
    public RectF getPromptBounds(int player, int offset) {
        float[] zero_zero = translateToScreen(0, 0);
        float[] one_one = translateToScreen(1, 1); 
        float[] last_last = translateToScreen(board.size - 1, board.size - 1);
        float[] lastminusone_lastminusone = translateToScreen(board.size - 2, board.size - 2);
        
        float board_x1 = zero_zero[0] - (one_one[0] - zero_zero[0]) / 2;
        float board_x2 = last_last[0] + (last_last[0] - lastminusone_lastminusone[0]) / 2;

        int yMargin = (int) (height * 0.265f);
        int xMargin = (int) (10); //board_x1 * 0.265f);
        
//        if(!drawSides) 
        {
            yMargin = (int) (border + scale * 2);
            xMargin = 5;
        }

//        if(player == 1) {
//            return new RectF(xMargin - offset, yMargin - offset, board_x1 - xMargin / 3 + offset, height - yMargin + offset);
//        }
//        return new RectF(board_x2 + xMargin / 3 - offset, yMargin - offset, width - xMargin + offset, height - yMargin + offset);
        if (player == 1) {
            return new RectF(board_x1 - scale * 2, yMargin - offset, board_x1 - xMargin + offset, height - yMargin + offset);
        }
        return new RectF(board_x2 + xMargin - offset, yMargin - offset, board_x2 + scale * 2, height - yMargin + offset);
    }
    
    public RectF getOKBounds(int player, int offset) {
        float[] zero_zero = translateToScreen(0, 0);
        float[] one_one = translateToScreen(1, 1); 
        float[] last_last = translateToScreen(board.size - 1, board.size - 1);
        float[] lastminusone_lastminusone = translateToScreen(board.size - 2, board.size - 2);
        
        float board_x1 = zero_zero[0] - (one_one[0] - zero_zero[0]) * 3;
        float board_x2 = last_last[0] + (last_last[0] - lastminusone_lastminusone[0]) * 3;

        int yMargin = (int) (height * 0.265f);
        int xMargin = (int) (10); //board_x1 * 0.265f);
        
//        if(!drawSides) 
        {
            yMargin = (int) (border + scale * 4);
            xMargin = 5;
        }

//        if(player == 1) {
//            return new RectF(xMargin - offset, yMargin - offset, board_x1 - xMargin / 3 + offset, height - yMargin + offset);
//        }
//        return new RectF(board_x2 + xMargin / 3 - offset, yMargin - offset, width - xMargin + offset, height - yMargin + offset);
        if (player == 1) {
            return new RectF(board_x1 - scale * 4, height / 2 + 3, board_x1 - xMargin + offset, height - yMargin + offset);
        }
        return new RectF(board_x2 + xMargin - offset, yMargin - offset, board_x2 + scale * 4, height / 2 - 3);
    }
    
    public RectF getCancelBounds(int player, int offset) {
        float[] zero_zero = translateToScreen(0, 0);
        float[] one_one = translateToScreen(1, 1); 
        float[] last_last = translateToScreen(board.size - 1, board.size - 1);
        float[] lastminusone_lastminusone = translateToScreen(board.size - 2, board.size - 2);
        
        float board_x1 = zero_zero[0] - (one_one[0] - zero_zero[0]) * 3;
        float board_x2 = last_last[0] + (last_last[0] - lastminusone_lastminusone[0]) * 3;

        int yMargin = (int) (height * 0.265f);
        int xMargin = (int) (10); //board_x1 * 0.265f);
        
//        if(!drawSides) 
        {
            yMargin = (int) (border + scale * 4);
            xMargin = 5;
        }

//        if(player == 1) {
//            return new RectF(xMargin - offset, yMargin - offset, board_x1 - xMargin / 3 + offset, height - yMargin + offset);
//        }
//        return new RectF(board_x2 + xMargin / 3 - offset, yMargin - offset, width - xMargin + offset, height - yMargin + offset);
        if (player == 1) {
            return new RectF(board_x1 - scale * 4, yMargin - offset, board_x1 - xMargin + offset, height / 2 - 3);
        }
        return new RectF(board_x2 + xMargin - offset, height / 2 + 3, board_x2 + scale * 4, height - yMargin + offset);
    }
    
    public int[] translateToBoard(float x, float y) {
        x -= xOffset;
        y -= yOffset;
        
//        x -= scale / 2;
//        y -= scale / 2;
        
        x -= border;
        y -= border;
        
        int boardX = (int) (x / scale);
        int boardY = (int) (y / scale);
        return new int[]{boardX, boardY};
    }
    
    public float[] translateToScreen(int x, int y) {
        float screenX = x * scale;
        float screenY = y * scale;

        screenX += border;
        screenY += border;
        
        screenX += scale / 2;
        screenY += scale / 2;
        
        screenX += xOffset;
        screenY += yOffset;
        
        return new float[]{screenX, screenY};
    }

    public boolean setPlacingPegLoc(float x, float y, float origX, float origY) {
        int[] boardLoc = translateToBoard(x, y);
        touch = new PointF(origX, origY);
        shadowPegLoc = new PointF(x, y);
        if(boardLoc != null && boardLoc[0] >= 0 && boardLoc[0] < board.size && boardLoc[1] >= 0 && boardLoc[1] < board.size) {
            float[] newLoc = translateToScreen(boardLoc[0], boardLoc[1]);
            placingPegLoc = new PointF(newLoc[0], newLoc[1]);
            if(lastPlacingBoardLoc == null || lastPlacingBoardLoc[0] != boardLoc[0] || lastPlacingBoardLoc[1] != boardLoc[1]) {
            	lastPlacingBoardLoc = boardLoc;
            	return true;
            }
        }
        else {
        	if(lastPlacingBoardLoc != null) {
        		placingPegLoc = null;
	        	lastPlacingBoardLoc = null;
	        	return true;
        	}
        }
        
        return (lastPlacingBoardLoc != null);
    }
}
