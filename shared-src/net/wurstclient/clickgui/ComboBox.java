/*
 * Copyright � 2014 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.clickgui;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.wurstclient.WurstClient;
import net.wurstclient.font.Fonts;
import net.wurstclient.settings.ModeSetting;

public final class ComboBox extends Component
{
	private final ModeSetting setting;
	private final int popupWidth;
	private ComboBoxPopup popup;
	
	public ComboBox(ModeSetting setting)
	{
		this.setting = setting;
		
		FontRenderer fr = Fonts.segoe18;
		popupWidth = Arrays.stream(setting.getModes())
			.mapToInt(m -> fr.getStringWidth(m)).max().getAsInt();
		
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton)
	{
		if(mouseX < getX() + getWidth() - popupWidth - 15)
			return;
		
		if(mouseButton == 0)
		{
			if(popup != null && !popup.isClosing())
			{
				popup.close();
				popup = null;
				return;
			}
			
			popup = new ComboBoxPopup(this);
			ClickGui gui = WurstClient.INSTANCE.getGui();
			gui.addPopup(popup);
			
		}else if(mouseButton == 1 && (popup == null || popup.isClosing()))
			setting.setSelected(setting.getDefaultSelected());
	}
	
	@Override
	public void render(int mouseX, int mouseY)
	{
		ClickGui gui = WurstClient.INSTANCE.getGui();
		float[] bgColor = gui.getBgColor();
		float[] acColor = gui.getAcColor();
		
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x2 - 11;
		int x4 = x3 - popupWidth - 4;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		int scroll = getParent().isScrollingEnabled()
			? getParent().getScrollOffset() : 0;
		boolean hovering = mouseX >= x1 && mouseY >= y1 && mouseX < x2
			&& mouseY < y2 && mouseY >= -scroll
			&& mouseY < getParent().getHeight() - 13 - scroll;
		boolean hText = hovering && mouseX < x4;
		boolean hBox = hovering && mouseX >= x4;
		
		// tooltip
		if(hText)
			gui.setTooltip(null);
		
		// background
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2], 0.5F);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x1, y1);
		GL11.glVertex2i(x1, y2);
		GL11.glVertex2i(x4, y2);
		GL11.glVertex2i(x4, y1);
		GL11.glEnd();
		
		// box
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2], hBox ? 0.75F : 0.5F);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x4, y1);
		GL11.glVertex2i(x4, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y1);
		GL11.glEnd();
		GL11.glColor4f(acColor[0], acColor[1], acColor[2], 0.5F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2i(x4, y1);
		GL11.glVertex2i(x4, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y1);
		GL11.glEnd();
		
		// separator
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2i(x3, y1);
		GL11.glVertex2i(x3, y2);
		GL11.glEnd();
		
		double xa1 = x3 + 1;
		double xa2 = (x3 + x2) / 2.0;
		double xa3 = x2 - 1;
		double ya1;
		double ya2;
		
		if(popup != null && !popup.isClosing())
		{
			ya1 = y2 - 3.5;
			ya2 = y1 + 3;
			GL11.glColor4f(hBox ? 1 : 0.85F, 0, 0, 1);
		}else
		{
			ya1 = y1 + 3.5;
			ya2 = y2 - 3;
			GL11.glColor4f(0, hBox ? 1 : 0.85F, 0, 1);
		}
		
		// arrow
		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glVertex2d(xa1, ya1);
		GL11.glVertex2d(xa3, ya1);
		GL11.glVertex2d(xa2, ya2);
		GL11.glEnd();
		
		// outline
		GL11.glColor4f(0.0625F, 0.0625F, 0.0625F, 0.5F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2d(xa1, ya1);
		GL11.glVertex2d(xa3, ya1);
		GL11.glVertex2d(xa2, ya2);
		GL11.glEnd();
		
		// setting name
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		FontRenderer fr = Fonts.segoe18;
		fr.drawString(setting.getName(), x1, y1 - 1, 0xf0f0f0);
		fr.drawString(setting.getSelectedMode(), x4 + 2, y1 - 1, 0xf0f0f0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	@Override
	public int getDefaultWidth()
	{
		return Fonts.segoe18.getStringWidth(setting.getName()) + popupWidth
			+ 17;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 11;
	}
	
	private static class ComboBoxPopup extends Popup
	{
		public ComboBoxPopup(ComboBox owner)
		{
			super(owner);
			setWidth(getDefaultWidth());
			setHeight(getDefaultHeight());
			setX(owner.getWidth() - getWidth());
			setY(owner.getHeight());
		}
		
		@Override
		public void handleMouseClick(int mouseX, int mouseY, int mouseButton)
		{
			if(mouseButton != 0)
				return;
			
			String[] values = ((ComboBox)getOwner()).setting.getModes();
			int yi1 = getY() - 11;
			for(String value : values)
			{
				if(value.equalsIgnoreCase(
					((ComboBox)getOwner()).setting.getSelectedMode()))
					continue;
				
				yi1 += 11;
				int yi2 = yi1 + 11;
				if(mouseY < yi1 || mouseY >= yi2)
					continue;
				
				((ComboBox)getOwner()).setting
					.setSelected(((ComboBox)getOwner()).setting.indexOf(value));
				close();
				break;
			}
		}
		
		@Override
		public void render(int mouseX, int mouseY)
		{
			ClickGui gui = WurstClient.INSTANCE.getGui();
			float[] bgColor = gui.getBgColor();
			float[] acColor = gui.getAcColor();
			
			int x1 = getX();
			int x2 = x1 + getWidth();
			int y1 = getY();
			int y2 = y1 + getHeight();
			
			boolean hovering =
				mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2;
			if(hovering)
				gui.setTooltip(null);
			
			// outline
			GL11.glColor4f(acColor[0], acColor[1], acColor[2], 0.5F);
			GL11.glBegin(GL11.GL_LINE_LOOP);
			GL11.glVertex2i(x1, y1);
			GL11.glVertex2i(x1, y2);
			GL11.glVertex2i(x2, y2);
			GL11.glVertex2i(x2, y1);
			GL11.glEnd();
			
			String[] values = ((ComboBox)getOwner()).setting.getModes();
			int yi1 = y1 - 11;
			for(String value : values)
			{
				if(value.equalsIgnoreCase(
					((ComboBox)getOwner()).setting.getSelectedMode()))
					continue;
				
				yi1 += 11;
				int yi2 = yi1 + 11;
				boolean hValue = hovering && mouseY >= yi1 && mouseY < yi2;
				
				// background
				GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2],
					hValue ? 0.75F : 0.5F);
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex2i(x1, yi1);
				GL11.glVertex2i(x1, yi2);
				GL11.glVertex2i(x2, yi2);
				GL11.glVertex2i(x2, yi1);
				GL11.glEnd();
				
				// value name
				GL11.glColor4f(1, 1, 1, 1);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				FontRenderer fr = Fonts.segoe18;
				fr.drawString(value.toString(), x1 + 2, yi1 - 1, 0xf0f0f0);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
			}
		}
		
		@Override
		public int getDefaultWidth()
		{
			return ((ComboBox)getOwner()).popupWidth + 15;
		}
		
		@Override
		public int getDefaultHeight()
		{
			return (((ComboBox)getOwner()).setting.getModes().length - 1) * 11;
		}
	}
}
